# Centroid Finder Project

This project finds a color in an image or video and then figures out where the biggest group of that color is. It was originally for images and then we added video stuff too.

## What It Does

Basically the program:

1. Takes in an image or video
2. Looks for pixels close to a target color
3. Turns those pixels into a black and white image
4. Finds connected groups
5. Finds the centroid of the biggest group
6. Writes results to files

For images it makes `binarized.png` and `groups.csv`.

For videos it makes a CSV file with the centroid for each second.

## Files

Most of the Java code is in:

```text
processor/src/main/java
```

Tests are in:

```text
processor/src/test/java
```

There is also a server folder:

```text
server
```

The server is for uploading videos and running the Java processor from an API. It is kind of extra.

## How To Run Tests

Go into the processor folder:

```bash
cd processor
```

Then run:

```bash
mvn test
```

## How To Build

From the processor folder:

```bash
mvn -DskipTests package
```

This makes the video processor jar in:

```text
processor/target/videoprocessor.jar
```

## Image Mode

Run the image app with Maven:

```bash
mvn exec:java -Dexec.mainClass=ImageSummaryApp -Dexec.args="sampleInput/squares.jpg FFA200 164"
```

This should make:

```text
binarized.png
groups.csv
```

The groups CSV is:

```text
size,x,y
```

## Video Mode

After building the jar, run:

```bash
java -jar target/videoprocessor.jar input.mp4 output.csv FFA200 164
```

The video CSV rows are:

```text
seconds,x,y
```

If it cannot find the centroid for a second it uses:

```text
-1,-1
```

## Server

The server is in the `server` folder. It uses Express and multer.

It has this route:

```text
POST /api/videos/centroids
```

It needs:

- a video file
- targetColor
- threshold

Then it gives back a job id and a download path.

## Main Classes

Some important classes are:

- `ImageSummaryApp`
- `VideoSummaryApp`
- `DistanceImageBinarizer`
- `DfsBinaryGroupFinder`
- `EuclideanColorDistance`
- `LargestCentroidFinder`
- `SecondBySecondVideoProcessor`
- `VideoArgumentsParser`

## Notes

The hardest part is probably understanding how the image gets turned into a binary array and then how DFS finds the connected groups.

Also the video version just does the image logic over and over on video frames.

## Improvements

Some things that could still be better:

- more tests
- better error messages
- better server cleanup
- maybe faster DFS
- better README

That is basically it.
