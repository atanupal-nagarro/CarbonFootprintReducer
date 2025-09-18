**What explored (code wise)?**



1\. Used OPENCAGE API to get location from Lat/Long of picture (Limitation - max 2500 request per day)



2\. Compressing and resizing to 1024x768



3\. ImageIO to compress images



4\. JpegImageMetadata to preserve exif info including GPS info



5\. Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

   GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class); GeoLocation - to get location







**Logic used to save carbon footprint**



Total space freed

Carbon footprint saved 0.06 g CO₂ per MB (The Shift Project, 2019)

Total carbon footprint saved





**Output of the code**



The pictures are present in the Pictures folder at the root

Choose an option:

1\. Compress all jpg images and delete original

2\. Delete all images of a specific city

3\. Delete all images of a specific country

4\. Exit

Enter your choice: 1



*Example*

*Directory size before compress : 15.58 MB*

*Directory size after compress : 1.98 MB*

*Compression achieved: 87.2756%*

*Space saved : 13.59 MB*

*Carbon footprint saved 0.06 g CO₂ per MB  (The Shift Project, 2019)*

*Total carbon footprint saved 0.8156445121765137 g*





The pictures are present in the Pictures folder at the root

Choose an option:

1\. Compress all jpg images and delete original

2\. Delete all images of a specific city

3\. Delete all images of a specific country

4\. Exit

Enter your choice: 3



*Example*

*Enter country name: India*

*1. Coordinates: 32.186613888888886, 76.36836944444444*

*Country: India*

*File: Pictures\\Dha1.jpg is of country India, hence deleting*

………



Total space freed : 8.99 MB

Carbon footprint saved 0.06 g CO₂ per MB (The Shift Project, 2019)

Total carbon footprint saved 0.5393867683410645 g

