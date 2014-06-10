package com.intellij.velocity.psi.files;

import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.velocity.psi.VtlLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexey Chmutov
 */
public class VelocityPropertiesProvider {
    private static final String VELOCIMACRO_LIBRARY_PROPERTY = "velocimacro.library";
    private static final String FILE_RESOURCE_LOADER_PATH_PROPERTY = "file.resource.loader.path";

    private final PropertiesFile myPropertiesFile;
    @Nullable private final VirtualFile myRuntimeRoot;

    public VelocityPropertiesProvider(@NotNull PropertiesFile file, @Nullable VirtualFile runtimeRoot) {
        this.myPropertiesFile = file;
        this.myRuntimeRoot = runtimeRoot;
    }

    public VelocityPropertiesProvider(@NotNull PropertiesFile file) {
        this.myPropertiesFile = file;
        this.myRuntimeRoot = null;
    }

    @NotNull
    public List<VirtualFile> getResourceLoaderPathListBasedOn(@Nullable VirtualFile baseFile) {
        if(myRuntimeRoot != null) {
            baseFile = myRuntimeRoot;
        }
        if (baseFile == null) {
            return Collections.emptyList();
        }
        ArrayList<VirtualFile> res = new ArrayList<VirtualFile>();
        for (String loaderPath : getResourceLoaderPathList()) {
            VirtualFile loaderPathFile = baseFile.findFileByRelativePath(loaderPath);
            if (loaderPathFile != null) {
                res.add(loaderPathFile);
            }
        }
        return res;
    }

    public String[] getResourceLoaderPathList() {
        String value = getValue(FILE_RESOURCE_LOADER_PATH_PROPERTY);
        return value.length() == 0 ? new String[]{"."} : splitAndTrim(value);
    }

    private static String[] splitAndTrim(String values) {
        String[] array = values.split(",");
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    @NotNull
    public List<VtlFile> getVelocimacroLibraryListBasedOn(@Nullable VirtualFile baseFile) {
        if(myRuntimeRoot != null) {
          baseFile = myRuntimeRoot;
        }
        String[] libNames = getVelocimacroLibraryNames();
        if (baseFile == null || libNames.length == 0) {
            return Collections.emptyList();
        }

        final PsiManager manager = myPropertiesFile.getManager();
        ArrayList<VtlFile> res = new ArrayList<VtlFile>();
        for (VirtualFile loaderPathFile : getResourceLoaderPathListBasedOn(baseFile)) {
            for (int i = libNames.length - 1; i >= 0; i--) {
                VirtualFile libFile = loaderPathFile.findFileByRelativePath(libNames[i]);
                if (libFile == null) {
                    continue;
                }
                final FileViewProvider viewProvider = manager.findViewProvider(libFile);
                if (viewProvider == null) {
                    continue;
                }
                PsiFile libPsiFile = viewProvider.getPsi(VtlLanguage.INSTANCE);
                if (libPsiFile instanceof VtlFile) {
                    ContainerUtil.addIfNotNull((VtlFile) libPsiFile, res);
                }
            }
        }
        return res;
    }

    @NotNull
    public PropertiesFile getPropertiesFile() {
        return myPropertiesFile;
    }

    @NotNull
    public String[] getVelocimacroLibraryNames() {
        final String value = getValue(VELOCIMACRO_LIBRARY_PROPERTY);
        return value.length() == 0 ? ArrayUtil.EMPTY_STRING_ARRAY : splitAndTrim(value);
    }

    @NotNull
    private String getValue(@NotNull @NonNls String key) {
        Property resourceLoaderPathProp = myPropertiesFile.findPropertyByKey(key);
        if (resourceLoaderPathProp == null) {
            return "";
        }
        String val = resourceLoaderPathProp.getValue();
        return val != null ? val : "";
    }
}
