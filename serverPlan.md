1. npm init in server folder
2. Use a child process to run the jar command 
3. have an input/output folder for the results.
4. Use crypto to create randomUUID for jobIds. Should I make a check to see if the UUID already exists? It's very unlikely.
5. create express endpoints for serving the video, and getting the first frame of the video. Or maybe that's something I need to do in the frontend. I feel like it might be wasted resources to query for a single frame when it could be done at the client.