package de.fuchscrafter.zipmc;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipMc extends JavaPlugin {

    public String prefix = "[ZipMC] ";
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        String worldName = this.getConfig().getString("world-name");
        Bukkit.getLogger().info(prefix + "World '" + worldName + "' will be zipped on server shutdown.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        String worldName = this.getConfig().getString("world-name");
        String zipName = worldName + "-compressed.zip";

        String basePath = this.getServer().getWorldContainer().getAbsolutePath().toString();
        String worldPath =  basePath + "/" + worldName + "/";

        String zipPath = basePath + "/" + zipName;


        Bukkit.getLogger().info(prefix + "World path:" + worldPath);
        Bukkit.getLogger().info(prefix + "Zip Path: " + zipPath);

        // Modified version of https://www.baeldung.com/java-compress-and-uncompress
        String sourceFile = worldPath;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(zipPath);
        } catch (FileNotFoundException e) {
            Bukkit.getLogger().severe(prefix + "Path not found! Stack trace:");
            throw new RuntimeException(e);
        } finally {
            assert fos != null;
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            File fileToZip = new File(sourceFile);
            try {
                zipFile(fileToZip, fileToZip.getName(), zipOut);
            } catch (IOException e) {
                Bukkit.getLogger().severe(prefix + "Error whilst zipping file! Stack trace:");
                throw new RuntimeException(e);
            }

            try {
                zipOut.close();
                fos.close();
            } catch (IOException e) {
                Bukkit.getLogger().severe(prefix + "Error whilst closing ZIP File! Stack trace:");
                throw new RuntimeException(e);
            }

            Bukkit.getLogger().info(prefix + "World '" + worldName + "' zipped to '" + zipName + "'!");
        }

    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws java.io.IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            assert children != null;
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
}
