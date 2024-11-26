async function performSearch(query, isDirectorySearch = false) {
    let resultsDiv;
    
    if (isDirectorySearch) {
        resultsDiv = document.getElementById("directoryResults");
    } else {
        resultsDiv = document.getElementById("gameResults");
    }

    resultsDiv.innerHTML = "Searching...";

    // determine the correct URL based on search type 
    const searchUrl = isDirectorySearch ? "http://localhost:8080/directory/search" : "http://localhost:8080/search";

    try {
        const response = await fetch(searchUrl, {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: query
        });

        if (response.ok) {
            const data = await response.text();
            resultsDiv.innerHTML = "";  
            const preFormattedText = document.createElement('pre');
            preFormattedText.textContent = data; 
            resultsDiv.appendChild(preFormattedText);
        } else {
            resultsDiv.textContent = "Error: Unable to fetch results.";
        }
    } catch (error) {
        resultsDiv.textContent = `Error: ${error.message}`;
    }
}

document.getElementById("searchButton").addEventListener("click", async () => {
    const query = document.getElementById("searchBox").value;
    console.log(`Button clicked. Searching for: ${query}`); 
    const isDirectorySearch = window.location.pathname.includes("/directory");
    await performSearch(query, isDirectorySearch);
});
