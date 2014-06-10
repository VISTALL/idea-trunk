//test for inspection InfiniteRecursion
function foo() {
  foo();
}

function loadAllFolderFiles(folderName) {
  var folder = fso.GetFolder(folderName);
  var fd = new Enumerator(folder.Files);
  var fileNameMask = /\.ht(m|ml)$/i;

  for (; !fd.atEnd(); fd.moveNext()) {
    if (fd.item().Name.match(fileNameMask)) {
      fileSet[fileSet.length + 1] = fd.item(i).Path;
    }
  }

  var fe = new Enumerator(folder.SubFolders);

  for (; !fe.atEnd(); fe.moveNext()) {
    loadAllFolderFiles(fe.item().Path);
  }
}

function findTrParentNode(node) {
    if(node == null) {
        return null;
    }
    if("TR" == node.tagName) {
        return node;
    }
    return findTrParentNode(node.parentNode);
}