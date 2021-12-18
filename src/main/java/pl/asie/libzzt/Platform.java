package pl.asie.libzzt;

import lombok.Builder;
import lombok.Data;

@SuppressWarnings("ConstantConditions")
@Data
@Builder
public class Platform {
    private final boolean usesBoard;
    private final int boardWidth;
    private final int boardHeight;
    @Builder.Default
    private final int defaultBoardWidth = -1;
    @Builder.Default
    private final int defaultBoardHeight = -1;
    @Builder.Default
    private final int maxBoardSize = -1; // incl. size byte
    @Builder.Default
    private final int maxStatCount = -1;
    @Builder.Default
    private final boolean doubleWide = false;
    @Builder.Default
    private final boolean maxStatCountIsActual = false;
    @Builder.Default
    private final boolean supportsBlinking = true;
    private final ElementLibrary library;

    public static final Platform ZZT;
    public static final Platform SUPER_ZZT;
    public static final Platform MEGAZEUX;

    public int getActualMaxStatCount() {
        return maxStatCountIsActual ? maxStatCount : maxStatCount + 1;
    }

    public int getDefaultBoardWidth() {
        return defaultBoardWidth < 0 ? boardWidth : defaultBoardWidth;
    }

    public int getDefaultBoardHeight() {
        return defaultBoardHeight < 0 ? boardHeight : defaultBoardHeight;
    }

    static {
        ZZT = Platform.builder().usesBoard(true).boardWidth(60).boardHeight(25).maxBoardSize(20000 + 2).maxStatCount(150).library(ElementLibraryZZT.INSTANCE).build();
        SUPER_ZZT = Platform.builder().usesBoard(true).boardWidth(96).boardHeight(80).maxBoardSize(20000 + 2).maxStatCount(128).library(ElementLibrarySuperZZT.INSTANCE).doubleWide(true).build();
        MEGAZEUX = Platform.builder().usesBoard(false).boardWidth(65535).boardHeight(65535).defaultBoardWidth(80).defaultBoardHeight(25).supportsBlinking(false).library(ElementLibraryNull.INSTANCE).build();
    }
}
