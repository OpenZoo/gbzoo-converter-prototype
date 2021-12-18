package pl.asie.gbzooconv;

import java.util.List;

public final class GBZooUtils {
	private GBZooUtils() {

	}

	public static byte[] toByteArray(List<Integer> fullData) {
		byte[] fullDataByte = new byte[fullData.size()];
		for (int i = 0; i < fullData.size(); i++) {
			fullDataByte[i] = (byte) ((int) fullData.get(i));
		}
		return fullDataByte;
	}
}
