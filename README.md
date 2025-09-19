This Java Swing application created by EcoVision Team enables you to reduce carbon footprint by compression or deletion based on different factors:


1. Compress all images (JPG) and delete the original
The program makes the pictures smaller (1024x768 resolution) and compresses it and then removes the bigger, old ones.
This saves storage and reduces carbon footprint.

2. Delete all images of a specific city
If you give the name of a city, the program looks at the photo’s GPS location and deletes only those pictures taken in that city.

3. Delete all images of a specific country
Similar to the city option, but works at the country level.

4. Compress all videos and delete the original
Videos are made smaller (using FFmpeg) and the old large ones are deleted.

5. Delete all picture files older than a specific year
You enter a year (e.g., 2018).
The program deletes all photos taken before that year.

Exit
The program stops running.

Note:
All pictures are stored in a folder called Pictures present at the application root.
All videos are stored in a folder called Videos present at the application root.
- Fetch the **city name** from GPS coordinates (using the [OpenCage API](https://opencagedata.com/)).  
- Track **carbon footprint savings** (in grams of CO₂ per MB) when compressing or deleting media files.  
- Log all outputs (`System.out.println`) directly into the **UI** instead of console.  

---

## 🚀 Features
- **UI Built with Swing** → `JFrame`, `JPanel`, `JTextArea` for logs.  
- **Redirected Console Logs** → `System.out.println` automatically displays inside the UI.  
- **OpenCage API Integration** → Reverse geocoding of latitude/longitude to get city names.  
- **Video Compression with FFmpeg** → Compressed video stored in `Downloads/` while preserving metadata.  
- **File Management & Carbon Tracking**  
  - On **compression** → Calculates saved size and carbon emissions avoided.  
  - On **deletion** → Tracks carbon footprint reduction.  
- **Date Handling** → Uses `Calendar` for comparing file modification dates.  
- **Image Compression** → Uses `BitmapFactory` and custom downscaling logic.  

---

## 🛠️ Technologies Used
- **Java (JDK 11+)**  
- **Swing** (UI framework)  
- **Graphics2D** (for image compression & resizing)  
- **OpenCage API** (reverse geocoding)  
- **FFmpeg** (video compression)   
- **Calendar / Date Utilities** (file comparison & history tracking)  

---

## ⚙️ Setup & Execution

### 1. Prerequisites
- Install **Java JDK 11 or later**   
- Maven to be installed

### 2. Clone 
```
git clone https://github.com/atanupal-nagarro/CarbonFootprintReducer.git
cd CarbonFootprintReducer
```

### 3. Run Application

From the root where pom.xml is:

mvn clean compile

mvn exec:java -Dexec.mainClass="com.example.CarbonFootprintReducer.CarbonFootprintReducerUI"


---

## 🔑 Logic Implemented

1. **UI Initialization**
   - `MainUI` creates a `JFrame` with a `JTextArea` for logs.
   - Console logs are redirected (`System.setOut`) so `System.out.println` appears in the UI.

2. **GPS to City Conversion**
   - `CarbonFootprintReducer` queries OpenCage API using latitude & longitude.  
   - JSON response parsed → extracts `"city"` or `"Country"` field.

3. **Carbon Footprint Calculation**
   - Formula: **0.05 g CO₂ per MB saved** (The Shift Project, 2019).  
   - Compression/deletion calculates `(originalSize - newSize) / 1,000,000 * 0.06`.

4. **Video Compression**
   - `VideoCompressor` invokes FFmpeg.  
   - For Android integration, MediaCodec + FileDescriptor supported (scoped storage friendly).  

5. **Image Compression**
   - `ImageCompressor` uses `BitmapFactory.Options` with `inSampleSize` to avoid OOM errors.  
   - Final resizing handled with `Graphics2D`.

6. **Date Comparisons**
   - `Calendar` compares file last-modified timestamps with current time (for history & cleanup).


---
