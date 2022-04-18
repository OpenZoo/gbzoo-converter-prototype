package pl.asie.gbzooconv;

import pl.asie.libzzt.Board;
import pl.asie.libzzt.Platform;
import pl.asie.libzzt.World;
import pl.asie.libzzt.ZInputStream;
import pl.asie.libzzt.ZOutputStream;
import pl.asie.libzzt.oop.OopSound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Main {
    private static World readWorld(File file) throws Exception {
        World world = new World(Platform.ZZT);

        try (FileInputStream fis = new FileInputStream(file); ZInputStream zis = new ZInputStream(fis, world.getPlatform())) {
            world.readZ(zis);
        }

        return world;
    }

    public static void main(String[] args) throws Exception {
       if (args.length < 2) {
           System.out.println("Usage: gbzooconv INPUT.ZZT OUTPUT.BIN");
           System.exit(1);
       }

        GBZooConverter converter = new GBZooConverter(4, 256, 1);
        converter.addWorld(readWorld(new File("/home/asie/zzt_gbc/zzt/DRZEEBO.ZZT")));

        // For now, only convert the starting board.
        try (FileOutputStream fos = new FileOutputStream(args[1])) {
            converter.write(fos);
        }
    }
}
