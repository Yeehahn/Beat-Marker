// Triggers when the process button is clicked
// Then grabs data from backend and displays it
async function processFile(){
    let formData = new FormData();
    let mp3Input = document.getElementById("mp3-input");
    formData.append("file", mp3Input.files[0]);
    let button = document.getElementById("process-button");
    button.innerHTML = "Processing Right Now!";
    // Wait for the server to finish processing the POST request
    await fetch('http://localhost:8080', {
        method: "POST",
        body: formData,
    });

    // Now fetch the processed data
    const bpmResponse = await fetch('http://localhost:8080/bpm');
    const bpm = await bpmResponse.json();
    const fileInformationPar = document.getElementById("file-information");
    fileInformationPar.innerHTML = bpm;

    const clickedFileResponse = await fetch('http://localhost:8080/clickedFile', {
        responseType: 'blob'
    });
    const clickedFile = await clickedFileResponse.blob();

    let downloadLink = document.getElementById("downloadLink");
    let url = URL.createObjectURL(clickedFile);
    downloadLink.href = url;

    downloadLink.download = "clickedSong.mp3";
    downloadLink.innerHTML = "The Clicked File";

    button.innerHTML = "Process";

}