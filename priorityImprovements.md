# Priority Improvements

## Refactoring code

1. Move shared command-line parsing and validation logic out of the app classes. `ImageSummaryApp` and `VideoSummaryApp` both parse colors, thresholds, inputs, and outputs in similar ways, so a shared parser or validation helper would reduce duplication.

2. Separate the video-processing loop from JavaCV-specific setup. `SecondBySecondVideoProcessor` currently opens the grabber, converts frames, calculates centroids, and writes CSV rows. Splitting frame reading from CSV writing would make the class easier to test.

## Adding tests

1. Add more tests for `VideoArgumentsParser`, especially exact six-digit color validation, negative threshold rejection, empty strings, and very large threshold values.

2. Add tests for `DfsBinaryGroupFinder` with empty arrays, jagged arrays, invalid values other than 0 or 1, and larger connected shapes. The class documentation describes expected validation behavior, but the current implementation does not clearly enforce all of it.

## Improving error handling

1. Validate input paths before starting JavaCV. A missing or unreadable file should produce a direct message instead of relying on a lower-level exception.

2. Make output-file failures more explicit. If the CSV path cannot be created or written, the app should report that clearly instead of only showing a generic processing error.

## Writing documentation

1. Update the README so it explains the current Maven layout and video workflow, not only the original image-processing assignment waves.

2. Document the CSV output format for both image mode and video mode. For video output, explain that rows are `seconds,x,y` and that `-1,-1` means no centroid was found for that second.

## Improving performance

1. Use a boolean visited grid in the DFS group finder instead of a `HashSet<String>`. This should reduce memory use and speed up group detection on large images or video frames.

2. Avoid repeatedly binarizing the same image when both a binarized image and group list are needed. The image app currently computes the binary array directly, then the group finder can binarize again internally.

## Hardening security

1. In the Express server, validate and parse all request fields before spawning Java. The `targetColor` and `threshold` should both be checked and normalized.

2. Do not expose raw internal error messages from the server API. They may reveal filesystem paths, command details, or dependency information.

## Bug fixes

1. Fix the Express server route that calls the video processor. It reads `thresholdRaw` from the request body, but then passes `threshold`, which is not defined. That would make the endpoint fail before Java is even started.

2. Tighten threshold validation. The parser currently accepts any integer, including negative values, even though a negative color-distance threshold does not make sense for this program.

## Other

1. Add a `.gitignore` entry for generated outputs and build artifacts if one is not already present, such as `target/`, `.m2-local/`, uploaded files, and generated CSV/images.

2. Decide whether the Node server is part of the submitted project or just a future extension. If it is part of the project, it needs stronger validation, scripts, and documentation.
