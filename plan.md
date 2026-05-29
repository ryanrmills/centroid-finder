# Video Processor Plan

## Goal
Create a command line video processor:

`java -jar videoprocessor.jar inputPath outputCsv targetColor threshold`

The output CSV must contain:
1. Seconds since the start of the video.
2. X coordinate of the largest centroid at that second.
3. Y coordinate of the largest centroid at that second.

If no centroid exists at that second, write `-1,-1`.

## Reuse Strategy
Reuse existing centroid finder code instead of re-implementing:

- `EuclideanColorDistance`
- `DistanceImageBinarizer`
- `BinarizingImageGroupFinder`
- `DfsBinaryGroupFinder`

Each sampled video frame is converted to `BufferedImage` and fed into this existing pipeline.

## Object-Oriented Architecture
### Components
- `VideoSummaryApp`: entry point and dependency wiring.
- `VideoArgumentsParser`: validates and parses CLI args into a config object.
- `VideoProcessingConfig`: immutable config record for video run settings.
- `SecondBySecondVideoProcessor`: reads frames and writes one CSV row per second.
- `LargestCentroidFinder`: extracts largest centroid from the image group list.

### Data Flow
`CLI args`
-> `VideoArgumentsParser`
-> `VideoProcessingConfig`
-> `SecondBySecondVideoProcessor`
-> `LargestCentroidFinder`
-> existing centroid classes
-> CSV rows (`second,x,y`)

## Architecture Sketch
```text
VideoSummaryApp
  |
  +-- VideoArgumentsParser --> VideoProcessingConfig
  |
  +-- SecondBySecondVideoProcessor
        |
        +-- FFmpegFrameGrabber + Java2DFrameConverter
        |
        +-- LargestCentroidFinder
              |
              +-- BinarizingImageGroupFinder
                    |
                    +-- DistanceImageBinarizer
                    +-- DfsBinaryGroupFinder
        |
        +-- PrintWriter (CSV)
```

## Testing Plan
1. Unit test argument parsing (`VideoArgumentsParser`) for:
1. valid input
1. bad argument count
1. invalid hex color
1. invalid threshold
2. Unit test `LargestCentroidFinder` for:
1. empty groups returns fallback `(-1,-1)`
1. non-empty groups returns largest centroid
3. Integration validation:
1. package JAR with Maven
1. run jar command on local mp4
1. inspect CSV to verify `second,x,y` format and one row per second

## Validation Notes
Executed:

`java -jar target/videoprocessor.jar sampleInput/ensantina.mp4 /private/tmp/video-groups-test.csv FFA200 164`

Verified:
- command runs successfully
- output CSV is generated
- rows are second-based samples with centroid coordinates
