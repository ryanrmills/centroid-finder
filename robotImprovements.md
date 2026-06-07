# Robot Improvements

These are the AI-suggested improvements that seem most useful after reviewing the current Java video processor, the older image processor, the tests, and the optional Express server wrapper. I filtered out ideas that felt too large, too vague, or likely to distract from the assignment.

## Refactoring code

- Move shared command-line parsing and validation logic out of the app classes. `ImageSummaryApp` and `VideoSummaryApp` both parse colors, thresholds, inputs, and outputs in similar ways, so a shared parser or validation helper would reduce duplication.

- Separate the video-processing loop from JavaCV-specific setup. `SecondBySecondVideoProcessor` currently opens the grabber, converts frames, calculates centroids, and writes CSV rows. Splitting frame reading from CSV writing would make the class easier to test.

- Replace string-based visited keys in `DfsBinaryGroupFinder` with a structure that matches the image grid, such as a boolean visited array. This would make the code easier to read and avoid building many temporary strings while scanning pixels.

- Keep the server wrapper separate from the core Java processor. The Java code should stay usable from the command line even if the Node/Express upload API changes later.

## Bug fixes

- Fix the Express server route that calls the video processor. It reads `thresholdRaw` from the request body, but then passes `threshold`, which is not defined. That would make the endpoint fail before Java is even started.

- Tighten threshold validation. The parser currently accepts any integer, including negative values, even though a negative color-distance threshold does not make sense for this program.

- Validate the hex color more strictly in the Java parser. `Integer.parseInt(args[2], 16)` accepts values that are not exactly six hex digits, so short or oversized colors could slip through.

- Decide what should happen when an input video has no image frames. Right now the processor quietly creates an empty CSV, which may be confusing.

## Adding tests

- Add more tests for `VideoArgumentsParser`, especially exact six-digit color validation, negative threshold rejection, empty strings, and very large threshold values.

- Add tests for `DfsBinaryGroupFinder` with empty arrays, jagged arrays, invalid values other than 0 or 1, and larger connected shapes. The class documentation describes expected validation behavior, but the current implementation does not clearly enforce all of it.

- Add an integration-style test for the video CSV format if a small test video can be included. The expected output should confirm the `seconds,x,y` format and the `-1,-1` fallback when no centroid is found.

- Add server-side tests or at least manual test cases for upload validation: missing file, bad color, bad threshold, oversized upload, and successful CSV download.

## Improving error handling

- Avoid printing full stack traces for normal user mistakes like bad CLI arguments or invalid files. Clear messages are easier to use and less noisy.

- Make output-file failures more explicit. If the CSV path cannot be created or written, the app should report that clearly instead of only showing a generic processing error.

- Validate input paths before starting JavaCV. A missing or unreadable file should produce a direct message instead of relying on a lower-level exception.

- In the server, return safer error messages to clients. Internal Java or filesystem errors can be logged on the server without sending all details back in the HTTP response.

## Improving performance

- Use a boolean visited grid in the DFS group finder instead of a `HashSet<String>`. This should reduce memory use and speed up group detection on large images or video frames.

- Avoid repeatedly binarizing the same image when both a binarized image and group list are needed. The image app currently computes the binary array directly, then the group finder can binarize again internally.

- Consider processing fewer pixels for very large video frames if approximate centroids are acceptable. Downscaling frames before binarization could make long videos much faster, but it would trade accuracy for speed.

## Hardening security

- In the Express server, validate and parse all request fields before spawning Java. The `targetColor` and `threshold` should both be checked and normalized.

- Do not expose raw internal error messages from the server API. They may reveal filesystem paths, command details, or dependency information.

- Add cleanup for uploaded videos and generated CSV files. Without cleanup, users can fill disk space over time.

- Consider restricting uploaded file types and keeping the upload size limit as low as the real use case allows. The current 1 GB limit may be larger than necessary.

## Writing documentation

- Update the README so it explains the current Maven layout and video workflow, not only the original image-processing assignment waves.

- Document the exact command for running tests and building the shaded jar.

- Document the CSV output format for both image mode and video mode. For video output, explain that rows are `seconds,x,y` and that `-1,-1` means no centroid was found for that second.

- Add a short explanation of the main processing pipeline: parse arguments, read image or frame, binarize by color distance, find connected groups, choose the largest centroid, and write output.

## Other

- Add a `.gitignore` entry for generated outputs and build artifacts if one is not already present, such as `target/`, `.m2-local/`, uploaded files, and generated CSV/images.

- Decide whether the Node server is part of the submitted project or just a future extension. If it is part of the project, it needs stronger validation, scripts, and documentation.
