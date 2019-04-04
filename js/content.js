  async function content() {
	let base = window.location.pathname.substr(0,window.location.pathname.indexOf('download'));
	let content =  window.location.pathname.substr(window.location.pathname.indexOf('download/'));
	content=content.substr(0,content.lastIndexOf('/'))
	const response = await fetch(
	  'https://api.github.com/repos/Ericsson/papyrus-patches/contents/'+content+'?ref=gh-pages');
	const data = await response.json();
	let htmlString = '<ul>';
	htmlString += `<li><img src="${base}/img/folder.png"/> <a href="${base}${content}/..">..</a></li>`;
	for (let file of data) {
	  if (file.type == 'dir')
		htmlString += `<li><img src="${base}/img/folder.png"/> <a href="${base}${file.path}">${file.name}</a></li>`;
	}
	for (let file of data) {
	  if (file.name.indexOf('.html') == (file.name.length - 5) || file.name.indexOf('.bak') == (file.name.length - 4))
	  	continue;
	  if (file.type == 'file')
		  htmlString += `<li><img src="${base}/img/file.png"/> <a href="${base}${file.path}">${file.name}</a></li>`;
	}
	htmlString += '</ul>';
	document.getElementById('directory').innerHTML = htmlString;
      }
