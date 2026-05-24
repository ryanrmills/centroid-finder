# Video Library Decision

## 1) JavaCV (Selected)
- Maven: `org.bytedeco:javacv-platform:1.5.13`
- Why selected:
  - Good fit for frame-by-frame video analysis in Java.
  - Includes FFmpeg/OpenCV integrations, so codec and video IO support are strong.
  - Practical path to reuse existing image-processing logic on each video frame.

## 2) OpenCV (Direct Java Bindings)
- Maven: `org.bytedeco:opencv-platform:4.13.0-1.5.13`
- Why considered:
  - Very strong computer vision ecosystem and performance.
  - `VideoCapture` API is standard for reading video streams/files.
- Why not selected first:
  - JavaCV offers a simpler "all-in-one" experience for video decode + frame access in this project.

## 3) JCodec
- Maven: `org.jcodec:jcodec:0.2.5`
- Why considered:
  - Pure Java library (no native dependency setup).
  - Lightweight and easy to add.
- Why not selected first:
  - More limited codec support and capabilities compared with FFmpeg-backed options.

## 4) Xuggler (Legacy)
- Why considered:
  - Historically used for Java video processing.
- Why not selected:
  - Older/legacy state makes it less suitable for a new setup compared with JavaCV/OpenCV.
