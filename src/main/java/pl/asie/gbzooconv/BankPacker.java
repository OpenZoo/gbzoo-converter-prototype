package pl.asie.gbzooconv;

import lombok.Data;
import lombok.With;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankPacker {
	private static final int BANK_SIZE = 16384;

	@Data
	@With
	public static class PointerUpdateRequest {
		private final boolean far;
		private final byte[] array;
		private final byte[] ptrArray;
		private final int position;
	}

	private final int startingBank;
	private final int maxBanks;
	private List<List<byte[]>> arrays;
	private Map<Integer, List<byte[]>> bankData;
	private Map<byte[], List<PointerUpdateRequest>> pointerUpdateRequestByTargetArray;

	public BankPacker(int startingBank, int maxBanks) {
		this.startingBank = startingBank;
		this.maxBanks = maxBanks;
		this.arrays = new ArrayList<>();
		this.bankData = new HashMap<>();
		this.pointerUpdateRequestByTargetArray = new HashMap<>();
	}

	public void add(List<byte[]> arrays) {
		this.arrays.add(arrays);
	}

	public void add(byte[] array) {
		add(List.of(array));
	}

	public void updatePointer(PointerUpdateRequest request) {
		this.pointerUpdateRequestByTargetArray.computeIfAbsent(request.getArray(), (k) -> new ArrayList<>())
				.add(request);
	}

	public void updatePointer(byte[] pointerList, int position, byte[] arrayToPointTo, boolean far) {
		PointerUpdateRequest request = new PointerUpdateRequest(far, arrayToPointTo, pointerList, position);
		updatePointer(request);
	}

	public int getUsedSpace(List<byte[]> list) {
		return list != null ? list.stream().mapToInt(a -> a.length).sum() : 0;
	}

	public int getUsedSpace(int bank) {
		return getUsedSpace(bankData.get(bank));
	}

	public int getFreeSpace(int bank) {
		return BANK_SIZE - getUsedSpace(bank);
	}

	public void addToBank(int bank, byte[] array) {
		int pos = getUsedSpace(bank);
		List<byte[]> list = bankData.get(bank);
		if (list == null) {
			list = new ArrayList<>();
			bankData.put(bank, list);
		}
		list.add(array);
		int nearPtr = pos + BANK_SIZE;
		for (PointerUpdateRequest request : this.pointerUpdateRequestByTargetArray.getOrDefault(array, List.of())) {
			request.ptrArray[request.position] = (byte) (nearPtr & 0xFF);
			request.ptrArray[request.position + 1] = (byte) (nearPtr >> 8);
			if (request.far) {
				request.ptrArray[request.position + 2] = (byte) bank;
			}
		}
	}

	public void pack() {
		arrays.sort(Comparator.comparingInt(a -> -getUsedSpace(a)));

		for (List<byte[]> arrayList : arrays) {
			int arraysLength = getUsedSpace(arrayList);

			for (int i = startingBank; i < maxBanks; i++) {
				if (getFreeSpace(i) >= arraysLength) {
					addToBank(i, arrayList.get(0));
					for (int j = 1; j < arrayList.size(); j++) {
						addToBank(i, arrayList.get(j));
					}
					break;
				}
			}
		}

		this.arrays.clear();
	}

	public void write(OutputStream stream) throws IOException {
		int maximumBank = this.bankData.keySet().stream().mapToInt(a -> a).max().orElse(this.startingBank - 1);
		maximumBank |= (maximumBank >> 1);
		maximumBank |= (maximumBank >> 2);
		maximumBank |= (maximumBank >> 4);
		maximumBank |= (maximumBank >> 8);
		for (int i = startingBank; i <= maximumBank; i++) {
			List<byte[]> list = this.bankData.get(i);
			int pos = 0;
			if (list != null) {
				for (byte[] data : list) {
					stream.write(data);
					pos += data.length;
				}
			}
			while (pos < BANK_SIZE) {
				stream.write(0);
				pos++;
			}
		}
	}
}
