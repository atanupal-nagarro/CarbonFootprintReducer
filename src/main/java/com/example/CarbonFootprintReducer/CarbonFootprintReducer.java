package com.example.CarbonFootprintReducer;

import java.io.File;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.lang.GeoLocation;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import org.apache.commons.imaging.*;
import org.apache.commons.imaging.formats.jpeg.exif.*;
import org.apache.commons.imaging.formats.tiff.write.*;
import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import java.io.*;
import java.util.Iterator;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.FFmpegExecutor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import com.drew.metadata.exif.ExifSubIFDDirectory;
public class CarbonFootprintReducer {
    private static final String OPENCAGE_API_KEY = "7219c4482169454fac0b4d75ce6d7e6a";
    public static void main(String[] args) throws ImageProcessingException, IOException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("\n\n\nThe pictures are present in the Pictures folder at the root (Videos are present in Videos folder)");
                System.out.println("Choose an option:");
                System.out.println("1. Compress all images(jpg) and delete original");
                System.out.println("2. Delete all images of a specific city");
                System.out.println("3. Delete all images of a specific country");
                System.out.println("4. Compress all videos and delete original");
                System.out.println("5. Delete all picture files older than a specific year");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");
                int choice;
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // consume newline
                } else {
                    System.out.println("Invalid input. Please enter a number (1-6).");
                    scanner.nextLine(); // clear wrong input
                    continue; // restart loop
                }

                switch (choice) {
                    case 1:
                        compressAllImages("Pictures/");
                        break;
                    case 2:
                        System.out.print("Enter city name: ");
                        String city = scanner.nextLine();
                        deleteImagesByCity(city, "Pictures/");
                        break;
                    case 3:
                        System.out.print("Enter country name: ");
                        String country = scanner.nextLine();
                        deleteImagesByCountry(country, "Pictures/");
                        break;
                    case 4:
                        compressAllVideos("Videos/");
                        break;
                    case 5:
                        System.out.print("Enter year (e.g., 2022): ");
                        int year = scanner.nextInt();
                        scanner.nextLine(); // consume newline
                        deleteFilesOlderThanYear("Pictures/", year);
                        break;
                    case 6:
                        System.out.println("Exiting program...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

            public static void deleteFilesOlderThanYear (String folderPath,int year){
                int Picyear = 0;
                File folder = new File(folderPath);
                if (!folder.exists() || !folder.isDirectory()) {
                    System.out.println("Invalid folder path.");
                    return;
                }

                File[] files = folder.listFiles();
                if (files == null) {
                    System.out.println("No files found in the folder.");
                    return;
                }
                int deletedCount = 0;
                long size =0;
                for (File file : files) {
                    if (file.isFile()) {
                        try {
                            Path filePath = file.toPath();
                            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                            long creationTime = attrs.creationTime().toMillis();
                            Picyear = 100000;
                            try {
                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                                if (exifDirectory != null) {
                                    Date creationDate = exifDirectory.getDateOriginal(); // DateTimeOriginal tag
                                    if (creationDate != null) {
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(creationDate);
                                        Picyear = calendar.get(Calendar.YEAR);
                                        System.out.println(file.getName() + " EXIF creation year: " + Picyear);
                                    } else {
                                        System.out.println(file.getName() + "EXIF creation date not found.");
                                    }
                                } else {
                                    System.out.println(file.getName() + "No EXIF metadata found.");
                                }
                            } catch (Exception e) {
                                System.out.println(file.getName() + "Error reading EXIF metadata: " + e.getMessage());
                                e.printStackTrace();
                            }
                            if (Picyear < year) {
                                System.out.println("File deleted " + file.getName());
                                size = size + file.length();
                                if (file.delete()) {
                                    deletedCount++;
                                } else {
                                    System.out.println("Failed to delete: " + file.getName());
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Error reading attributes for file: " + file.getName());
                            e.printStackTrace();
                        }
                    }
                }
                System.out.println("Deleted " + deletedCount + " files created before " + year + ".");
                double sizeInMB = (double) size / (1024 * 1024);
                System.out.printf("Total space freed : %.2f MB%n", sizeInMB);
                System.out.println("Carbon footprint saved 0.06 g CO2 per MB (The Shift Project, 2019)");
                System.out.println("Total carbon footprint saved " + sizeInMB*0.06 + " g");
    }
    public static void compressAllImages(String folderPath) {
        File folder = new File(folderPath);
        long sizeInBytes = getDirectorySize(folder);
        double sizeInMB = (double) sizeInBytes / (1024 * 1024);
        System.out.printf("Directory size before compress : %.2f MB%n", sizeInMB);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
        if (files == null || files.length == 0) {
            System.out.println("No JPG images found in folder.");
            return;
        }
        for (File file : files) {
            try {
                BufferedImage originalImage = ImageIO.read(file);
                if (originalImage == null) continue;
                int originalWidth = originalImage.getWidth();
                int originalHeight = originalImage.getHeight();
                int targetWidth = Math.min(1024, originalWidth);
                int targetHeight = Math.min(768, originalHeight);
                // Maintain aspect ratio
                double aspectRatio = (double) originalWidth / originalHeight;
                if (originalWidth > targetWidth || originalHeight > targetHeight) {
                    if (aspectRatio > 1) {
                        targetHeight = (int) (targetWidth / aspectRatio);
                    } else {
                        targetWidth = (int) (targetHeight * aspectRatio);
                    }
                } else {
                    System.out.println("Skipping (already small): " + file.getName());
                    continue;
                }
                // Scale
                Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                BufferedImage compressedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = compressedImage.createGraphics();
                g2d.drawImage(scaledImage, 0, 0, null);
                g2d.dispose();
                // Write compressed image temporarily
                File tempFile = new File(file.getParent(), "temp_" + file.getName());
                try (OutputStream os = new FileOutputStream(tempFile)) {
                    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
                    ImageWriter writer = writers.next();
                    ImageWriteParam param = writer.getDefaultWriteParam();
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(0.85f); // adjust quality
                    ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                    writer.setOutput(ios);
                    writer.write(null, new IIOImage(compressedImage, null, null), param);
                    ios.close();
                    writer.dispose();
                }
                // ✅ Copy EXIF (including GPS) from old → new
                copyExifPureJava(file, tempFile);
                // Replace original
                if (file.delete()) {
                    tempFile.renameTo(file);
                    System.out.println("Compressed (EXIF preserved): " + file.getName());
                }

            } catch (Exception e) {
                System.out.println("Error processing: " + file.getName());
                e.printStackTrace();
            }
        }
        long sizeInBytes2 = getDirectorySize(folder);
        double sizeInMB2 = (double) sizeInBytes2 / (1024 * 1024);
        System.out.printf("Directory size after compress : %.2f MB%n", sizeInMB2);
        double saved = sizeInMB - sizeInMB2;
        double compressionPercentage = calculateCompressionPercentage(sizeInMB, sizeInMB2);
        System.out.printf("Compression achieved: %.4f%%%n", compressionPercentage);
        System.out.printf("Space saved : %.2f MB%n", saved);
        System.out.println("Carbon footprint saved 0.06 g CO2 per MB  (The Shift Project, 2019)");
        System.out.println("Total carbon footprint saved " + saved * 0.06 + " g");
    }
    public static double calculateCompressionPercentage(double inputSize, double outputSize) {
        if (inputSize <= 0) {
            throw new IllegalArgumentException("Input size must be greater than 0");
        }
        return ((inputSize - outputSize) / inputSize) * 100;
    }

public static void deleteImagesByCity(String city, String folderPath) throws ImageProcessingException, IOException {
    File folder = new File(folderPath);
    File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));
    if (files == null || files.length == 0) {
        System.out.println("No JPG images found in folder.");
        return;
    }
    int counter = 1;
    long size =0;
    for (File imageFile : files) {
        //File imageFile = file;
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
        GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);

        if (gpsDir != null) {
            GeoLocation location = gpsDir.getGeoLocation();
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                System.out.println(counter + ". Coordinates: " + lat + ", " + lng);
                counter = counter + 1;
                String getCity = getCityFromCoordinates(lat, lng);
                System.out.println("City: " + getCity);
                if (getCity.equalsIgnoreCase(city))
                {
                    System.out.println("File: " + imageFile + " is of city "  + getCity + ", hence deleting");
                    size = size + imageFile.length();
                    imageFile.delete();
                }
            } else {
                System.out.println("No GPS data found.");
            }
        } else {
            System.out.println("No GPS directory found.");
        }
    }
    double sizeInMB = (double) size / (1024 * 1024);
    System.out.printf("Total space freed : %.2f MB%n", sizeInMB);
    System.out.println("Carbon footprint saved 0.06 g CO2 per MB (The Shift Project, 2019)");
    System.out.println("Total carbon footprint saved " + sizeInMB*0.06 + " g");
}

    public static void deleteImagesByCountry(String country, String folderPath) throws ImageProcessingException, IOException {
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));

        if (files == null || files.length == 0) {
            System.out.println("No JPG images found in folder.");
            return;
        }
        int counter = 1;
        long size =0;
        for (File imageFile : files) {

            //File imageFile = file;
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);

            if (gpsDir != null) {
                GeoLocation location = gpsDir.getGeoLocation();
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    System.out.println(counter + ". Coordinates: " + lat + ", " + lng);
                    counter = counter + 1;
                    String getCountry = getCountryFromCoordinates(lat, lng);
                    System.out.println("Country: " + getCountry);
                    if (getCountry.equalsIgnoreCase(country))
                    {
                        System.out.println("File: " + imageFile + " is of country "  + country + ", hence deleting");
                        size = size+ imageFile.length();
                        imageFile.delete();
                    }
                } else {
                    System.out.println("No GPS data found.");
                }
            } else {
                System.out.println("No GPS directory found.");
            }
        }
        double sizeInMB = (double) size / (1024 * 1024);
        System.out.printf("Total space freed : %.2f MB%n", sizeInMB);
        System.out.println("Carbon footprint saved 0.06 g CO2 per MB (The Shift Project, 2019)");
        System.out.println("Total carbon footprint saved " + sizeInMB*0.06 + " g");

    }
    private static String getCityFromCoordinates(double lat, double lng) {
        try {
            String urlStr = String.format(
                "https://api.opencagedata.com/geocode/v1/json?q=%f+%f&key=%s",
                lat, lng, OPENCAGE_API_KEY
            );
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream responseStream = conn.getInputStream();

            Scanner scanner = new Scanner(responseStream).useDelimiter("\\A");
            //Scanner scanner = new Scanner(responseStream).useDelimiter("\A");
            String response = scanner.hasNext() ? scanner.next() : "";

            JSONObject json = new JSONObject(response);
            JSONObject components = json.getJSONArray("results")
                                        .getJSONObject(0)
                                        .getJSONObject("components");

            return components.optString("city", components.optString("town", components.optString("village", "Unknown")));
        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching city";
        }
    }
    private static String getCountryFromCoordinates(double lat, double lng) {
        try {
            String urlStr = String.format(
                    "https://api.opencagedata.com/geocode/v1/json?q=%f+%f&key=%s",
                    lat, lng, OPENCAGE_API_KEY
            );

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStream responseStream = conn.getInputStream();

            Scanner scanner = new Scanner(responseStream).useDelimiter("\\A");
            //Scanner scanner = new Scanner(responseStream).useDelimiter("\A");
            String response = scanner.hasNext() ? scanner.next() : "";

            JSONObject json = new JSONObject(response);
            JSONObject components = json.getJSONArray("results")
                    .getJSONObject(0)
                    .getJSONObject("components");

            return components.optString("country"); //, components.optString("town", components.optString("village", "Unknown")));
        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching city";
        }
    }

    public static long getDirectorySize(File dir) {
        if (dir == null || !dir.exists()) {
            return 0;
        }
        if (dir.isFile()) {
            return dir.length();
        }

        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                size += getDirectorySize(file);
            }
        }
        return size;
    }
    private static void copyExifPureJava(File source, File target) {
        try {
            // Read original metadata
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) Imaging.getMetadata(source);

            if (jpegMetadata != null) {
                TiffImageMetadata exif = jpegMetadata.getExif();
                if (exif != null) {
                    // ✅ Preserve all EXIF including GPS
                    TiffOutputSet outputSet = exif.getOutputSet();

                    // Extra safeguard: if GPS directory exists, copy it too
                    TiffImageMetadata.GPSInfo gpsInfo = exif.getGPS();
                    if (gpsInfo != null) {
                        double lat = gpsInfo.getLatitudeAsDegreesNorth();
                        double lon = gpsInfo.getLongitudeAsDegreesEast();
                        System.out.println("GPS found: " + lat + ", " + lon);
                    } else {
                        System.out.println("⚠️ Original image has no GPS info.");
                    }

                    // Now re-write EXIF into compressed file
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(target.toPath());
                    try (FileOutputStream fos = new FileOutputStream(target)) {
                        new ExifRewriter().updateExifMetadataLossless(imageBytes, fos, outputSet);
                    }
                } else {
                    System.out.println("⚠️ Original image has EXIF but no GPS.");
                }
            } else {
                System.out.println("⚠️ No EXIF metadata in original image.");
            }
        } catch (Exception e) {
            System.out.println("Failed to copy EXIF/GPS: " + e.getMessage());
        }
    }
    public static void compressAllVideos(String folderPath) throws IOException {
        File inputDir = new File(folderPath);
        File outputDir = new File(folderPath);
        if (!outputDir.exists()) outputDir.mkdirs();

        long sizeInBytes = getDirectorySize(inputDir);
        double sizeInMB = (double) sizeInBytes / (1024 * 1024);
        System.out.printf("Directory size before compress : %.2f MB%n", sizeInMB);

        FFmpeg ffmpeg = new FFmpeg("ffmpeg-8.0/bin/ffmpeg.exe");
        FFprobe ffprobe = new FFprobe("ffmpeg-8.0/bin/ffprobe.exe");

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        File[] videoFiles = inputDir.listFiles((dir, name) ->
                name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".avi"));

        if (videoFiles == null || videoFiles.length == 0) {
            System.out.println("No video files found.");
            return;
        }
        for (File video : videoFiles) {
            System.out.println("Video file: " + video.getName());

            FFmpegProbeResult probeResult;
            try {
                probeResult = ffprobe.probe(video.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Skipping corrupted file (probe failed): " + video.getName());
                continue;
            }
            int width = probeResult.getStreams().get(0).width;
            int height = probeResult.getStreams().get(0).height;
            // Create timestamped output file name
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String tempOutputName = timestamp + "_" + video.getName();
            String tempOutputPath = outputDir.getAbsolutePath() + File.separator + tempOutputName;
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(video.getAbsolutePath())
                    .overrideOutputFiles(true)
                    .addOutput(tempOutputPath)
                    .setFormat("mp4")
                    .setVideoCodec("libx264")
                    .setAudioCodec("aac")
                    .setAudioBitRate(128_000)
                    .setVideoBitRate(1_000_000)
                    .setPreset("fast")
                    .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                    /*.setVideoFilter(width > 1024 || height > 768
                            ? "scale='min(1024,iw)':'min(768,ih)':force_original_aspect_ratio=decrease"
                            : null)*/
                    .setVideoFilter("scale='min(1024,iw)':'min(768,ih)':force_original_aspect_ratio=decrease")
                    .done();
            try {
                executor.createJob(builder).run();
                System.out.println("Compressed successfully: " + video.getName());
                // Rename compressed file to original name
                File tempFile = new File(tempOutputPath);
                File finalFile = new File(outputDir, video.getName());
                // Delete original file
                if (video.delete()) {
                    System.out.println("Deleted original: " + video.getName());
                } else {
                    System.out.println("Failed to delete original: " + video.getName());
                }
                if (tempFile.renameTo(finalFile)) {
                    System.out.println("Renamed to original name: " + finalFile.getName());
                } else {
                    System.err.println("Failed to rename compressed file: " + tempOutputName);
                }
            } catch (Exception e) {
                System.err.println("Compression failed for: " + video.getName());
                e.printStackTrace();
            }
        }
        sizeInBytes = getDirectorySize(inputDir);
        double sizeInMB2 = (double) sizeInBytes / (1024 * 1024);
        System.out.printf("Directory size after compress : %.2f MB%n", sizeInMB2);
        double saved = sizeInMB - sizeInMB2;
        double compressionPercentage = calculateCompressionPercentage(sizeInMB, sizeInMB2);
        System.out.printf("Compression achieved: %.4f%%%n", compressionPercentage);
        System.out.printf("Space saved : %.2f MB%n", saved);
        System.out.println("Carbon footprint saved 0.06 g CO2 per MB  (The Shift Project, 2019)");
        System.out.println("Total carbon footprint saved " + saved * 0.06 + " g");
    }
}