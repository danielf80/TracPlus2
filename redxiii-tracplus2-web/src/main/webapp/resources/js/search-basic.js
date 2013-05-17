/**
 * Switch visibility of all search results 
 * @param type
 */
function switchVibility(type) {
	if (type == "none") {
		showAll();
	} else {
		hideAll(type);
	}
}

/**
 * Switch visibility of all search results (Show all DIV elements) 
 */
function showAll() {
	var result = document.getElementsByClassName('search-result-panel');
	for ( var ePanel in result) {
		if (ePanel.match("\\d+") == null) {
		} else {
			var eDivs = result[ePanel].getElementsByTagName('div');
			for ( var eDiv in eDivs) {
				if (eDiv.match("\\d+") == null) {
				} else {
					eDivs[eDiv].style.display = 'block';
				}
			}
		}
	}
}

/**
 * Switch visibility of all search results (Hide all DIV elements different of 'keepType')
 * @param type 
 */
function hideAll(keepType) {
	var result = document.getElementsByClassName('search-result-panel');
	for ( var ePanel in result) {
		if (ePanel.match("\\d+") == null) {
		} else {
			var eDivs = result[ePanel].getElementsByTagName('div');
			for ( var eDiv in eDivs) {
				if (eDiv.match("\\d+") == null) {
				} else if (eDivs[eDiv].id.indexOf(keepType) == 0) {
					eDivs[eDiv].style.display = 'block';
				} else {
					eDivs[eDiv].style.display = 'none';
				}
			}
		}

	}
}