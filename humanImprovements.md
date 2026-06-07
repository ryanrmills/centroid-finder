Move repeated command-line checking into one shared helper so both apps can use the same code.

Add more tests to make sure VideoArgumentsParser rejects bad colors and bad threshold values.

Show a clear error message when the app cannot create or write the CSV file.

Fix the server so it actually uses the uploaded threshold value when starting the video processor.