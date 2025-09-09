
# Beat-Marker
To run this program open the front end in a separate window in VS Code using a live server. 
Ideally the website would run on the client-end and the server would run independently but I do not have such capabilities currently.


## Folders
**frontend:** The front-end of the website.

**Where to find back-end**

Follow down the path src/main/java/com/example/demo to see the back-end.

**config:** Contains the config that creates a FileHolder object to be inserted into the service layer.

**controllers:** Contains the rest controller that interacts with the front-end

**domain:** Holds the file for the FileHolder object which receives the MultiPartFile from the front-end and converts and stores it into a useable mp3 file.

**onset:** The entirety of the onset-dection process used in order to detect the beats of the song. Utilizes fourier transforms and many forms of signal processing to eventually simplify the song into a sinusoidal wave where the peak of each wave is a beat.

**service:** The service layer between the frontend and backend.

**util:** Opens the server.
