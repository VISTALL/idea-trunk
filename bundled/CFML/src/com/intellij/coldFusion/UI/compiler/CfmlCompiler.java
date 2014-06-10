package com.intellij.coldFusion.UI.compiler;

import com.intellij.coldFusion.UI.facet.CfmlFacet;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.IOUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lera Nikolaenko
 * Date: 07.04.2009
 */
public class CfmlCompiler implements PackagingCompiler/*CopyingCompiler*/ {
    private static CfmlCompiler INSTANCE = new CfmlCompiler();

    private CfmlCompiler() {
    }

    public static CfmlCompiler getInstance() {
        return INSTANCE;
    }

    /*
    public VirtualFile[] getFilesToCopy(CompileContext context) {
        for (Module module : context.getCompileScope().getAffectedModules()) {
            CfmlFacet moduleFacet =
                (CfmlFacet)(FacetManager.getInstance(module).getFacetsByType(CfmlFacet.ID));
            CfmlFacetConfiguration.CfmlFacetState state = ((CfmlFacetConfiguration.CfmlFacetState)moduleFacet.getConfiguration().getState());
            String serverDirectory = state.getMyServerDirectory();
            Map<String, String> map = state.getMyResourceDirectories();
            // for a while only first string will be watched
            for (Map.Entry<String, String> entry : map.entrySet()) {
                return module.getModuleFile().getFileSystem().findFileByPath(entry.getKey()).getChildren();
            }

        }

        return new VirtualFile[0];
    }

    public String getDestinationPath(VirtualFile sourceFile) {
        for (Module module : context.getCompileScope().getAffectedModules()) {
            CfmlFacet moduleFacet =
                (CfmlFacet)(FacetManager.getInstance(module).getFacetsByType(CfmlFacet.ID));
            CfmlFacetConfiguration.CfmlFacetState state = ((CfmlFacetConfiguration.CfmlFacetState)moduleFacet.getConfiguration().getState());
            String serverDirectory = state.getMyServerDirectory();
            Map<String, String> map = state.getMyResourceDirectories();
            // for a while only first string will be watched
            for (Map.Entry<String, String> entry : map.entrySet()) {
                return module.getModuleFile().getFileSystem().findFileByPath(entry.getKey()).getChildren();
            }

        }



        return ApplicationManager.getApplication().getComponent(CfmlApplicationComponent.class).getTestServerDirectoryPath() +
                PathUtil.suggestFileName(sourceFile.getPath());
    }
    */
    public final void processOutdatedItem(CompileContext context, String url, @Nullable ValidityState state) {
        if (state != null) {
            final String destinationPath = ((DestinationFileInfo) state).getDestinationPath();
            new File(destinationPath).delete();
        }
    }

    private void addProcessingChildrenFiles(VirtualFile directory, List<VirtualFile> itemsList) {
        if (!directory.isDirectory()) {
            return;
        }
        for (VirtualFile file : directory.getChildren()) {
            if (file.isDirectory()) {
                addProcessingChildrenFiles(file, itemsList);
            } else {
                if (file.getFileType() == CfmlFileType.INSTANCE) {
                    itemsList.add(file);
                }
            }
        }
    }

    @NotNull
    public final ProcessingItem[] getProcessingItems(final CompileContext context) {
/*
      return ApplicationManager.getApplication().runReadAction(new Computable<ProcessingItem[]>() {
        public ProcessingItem[] compute() {
        }
      });
*/
        List<ProcessingItem> itemsList = new LinkedList<ProcessingItem>();

        for (Module module : context.getCompileScope().getAffectedModules()) {
            Collection<CfmlFacet> moduleFacet =
                    FacetManager.getInstance(module).getFacetsByType(CfmlFacet.ID);
            if (moduleFacet.size() != 1) {
                continue;
            }
            CfmlFacet facet = moduleFacet.iterator().next();
            String serverDirectory = facet.getConfiguration().getDeploymentDirectoryPath();
/*
            List<WebRoot> webRoots = facet.getConfiguration().getWebRoots(false);
            for (WebRoot webRoot : webRoots) {
                for (VirtualFile childFile : webRoot.getFile().getChildren())
                itemsList.add(new CopyItem(childFile, serverDirectory + webRoot.getRelativePath()));
            }
*/

            List<VirtualFile> files = new LinkedList<VirtualFile>();
            String ignoredPath = context.getProject().getBaseDir().getPath();
            addProcessingChildrenFiles(context.getProject().getBaseDir(), files);
            for (VirtualFile file : files) {
                int firstIndex = ignoredPath.length();
                int lastIndex = file.getPath().lastIndexOf("/");
                itemsList.add(new CopyItem(file, serverDirectory + file.getPath().substring(firstIndex, lastIndex)));
            }
            /*module.getModuleFile().getFileSystem().findFileByPath(entry.getKey()).getChildren()*/
        }
        return itemsList.toArray(new ProcessingItem[0]);
    }

    public ProcessingItem[] process(CompileContext context, ProcessingItem[] items) {
        final List<ProcessingItem> successfullyProcessed = new ArrayList<ProcessingItem>(items.length);
        for (ProcessingItem item : items) {
            final CopyItem copyItem = (CopyItem) item;
            final String fromPath = copyItem.getSourcePath();
            final String toPath = copyItem.getDestinationPath();
            try {
                File toFolder = new File(toPath);
                toFolder.mkdirs();

                File toFile = new File(toPath + "/" + copyItem.getFile().getName());
                FileUtil.copy(new File(fromPath), toFile);
                successfullyProcessed.add(copyItem);
            }
            catch (IOException e) {
                context.addMessage(
                        CompilerMessageCategory.ERROR,
                        "Error copying from " + fromPath + " to " + toPath + ": " + e.getMessage(),
                        null, -1, -1
                );
            }
        }
        return successfullyProcessed.toArray(new ProcessingItem[successfullyProcessed.size()]);
    }

    @NotNull
    public String getDescription() {
        return "Cold Fusion Compiler Descriptor";
    }

    public boolean validateConfiguration(CompileScope scope) {
        return true;
    }

    public ValidityState createValidityState(DataInput in) throws IOException {
        return new DestinationFileInfo(IOUtil.readString(in), true);
    }

    private static class CopyItem implements FileProcessingCompiler.ProcessingItem {
        private final VirtualFile myFile;
        private final DestinationFileInfo myInfo;
        private final String mySourcePath;

        public CopyItem(VirtualFile file, String destinationPath) {
            myFile = file;
            mySourcePath = file.getPath().replace('/', File.separatorChar);
            myInfo = new DestinationFileInfo(destinationPath, new File(destinationPath).exists());
        }

        @NotNull
        public VirtualFile getFile() {
            return myFile;
        }

        public ValidityState getValidityState() {
            return myInfo;
        }

        public String getSourcePath() {
            return mySourcePath;
        }

        public String getDestinationPath() {
            return myInfo.getDestinationPath();
        }
    }

    private static class DestinationFileInfo implements ValidityState {
        private final String destinationPath;
        private final boolean myFileExists;

        public DestinationFileInfo(String destinationPath, boolean fileExists) {
            this.destinationPath = destinationPath;
            myFileExists = fileExists;
        }

        public boolean equalsTo(ValidityState otherState) {
            if (!(otherState instanceof DestinationFileInfo)) {
                return false;
            }
            DestinationFileInfo destinationFileInfo = (DestinationFileInfo) otherState;
            return (myFileExists == destinationFileInfo.myFileExists) && (destinationPath.equals(destinationFileInfo.destinationPath));
        }

        public void save(DataOutput out) throws IOException {
            IOUtil.writeString(destinationPath, out);
        }

        public String getDestinationPath() {
            return destinationPath;
        }
    }


}
