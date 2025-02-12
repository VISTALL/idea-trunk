/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.ruby.ruby.cache;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.cache.fileCache.RubyFilesCache;
import org.jetbrains.plugins.ruby.ruby.cache.index.DeclarationsIndex;
import org.jetbrains.plugins.ruby.ruby.cache.info.RFileInfo;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualElement;
import org.jetbrains.plugins.ruby.ruby.cache.psi.containers.RVirtualClass;
import org.jetbrains.plugins.ruby.ruby.cache.psi.containers.RVirtualContainer;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.presentation.RClassPresentationUtil;
import org.jetbrains.plugins.ruby.ruby.scope.SearchScope;
import org.jetbrains.plugins.ruby.ruby.scope.SearchScopeUtil;
import org.jetbrains.plugins.ruby.support.utils.RModuleUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 06.08.2007
 */

/**
 * Utils for sdk, ruby and rails modules cache
 */
public class RCacheUtil {

    /**
     * Returns array of 2 elements
     * 1. module cache for file
     * 2. skd cache for file (first found)
     * According search scope array elements can be null.
     *
     * @param file    Ruby file
     * @param scope   Search scope
     * @param project Project
     * @return Caches
     */
    @NotNull
    public static RubyFilesCache[] getCachesByFile(@NotNull final VirtualFile file,
                                                  @NotNull final SearchScope scope,
                                                  @NotNull final Project project) {
        final RubyFilesCache[] caches = new RubyFilesCache[2];
        final Module module = getModuleByFile(file, project);

        final RubySdkCachesManager sdkCachesManager = RubySdkCachesManager.getInstance(project);
        if (RModuleUtil.hasRubySupport(module)) {
            //noinspection ConstantConditions
            if (scope.isSearchInModuleContent(module)) {
                final RubyModuleCachesManager manager = getCachesManager(module);
                caches[0] = manager == null ? null : manager.getFilesCache();
            }
            if (scope.isSearchInSDKLibraries()) {
                final ProjectJdk sdk = RModuleUtil.getModuleOrJRubyFacetSdk(module);
                if (sdk != null) {
                    caches[1] = sdkCachesManager.getSdkFilesCache(sdk);
                }
            }
        } else {
            if (scope.isSearchInSDKLibraries()) {
                caches[1] = sdkCachesManager.getFirstCacheByFile(file);
            }
        }
        return caches;
    }

    /**
     * Returns array of 2 elements
     * 1. module word index for file
     * 2. skd word index for file (first found)
     * According search scope array elements can be null.
     *
     * @param file    Ruby file
     * @param scope   Search scope
     * @param project Project
     * @return WordIndexes
     */
    @NotNull
    public static DeclarationsIndex[] getDeclarationsIndexByFile(@NotNull final VirtualFile file,
                                                                 @NotNull final SearchScope scope,
                                                                 @NotNull final Project project) {
        final RubyFilesCache[] caches = getCachesByFile(file, scope, project);
        return new DeclarationsIndex[]{
                caches[0]!=null ? caches[0].getDeclarationsIndex() : null,
                caches[1]!=null ? caches[1].getDeclarationsIndex() : null
        };
    }

    /**
     * @param module Rails or Ruby module
     * @return Caches manager
     */
    @Nullable
    @Deprecated
    public static RubyModuleCachesManager getCachesManager(@NotNull final Module module) {
//        if (RailsUtil.isRailsModule(module)) {
//            return RailsModuleCachesManager.getInstance(module);
//        } else
//        if (RubyUtil.isRubyModuleType(module) || JRubyUtil.hasJRubySupport(module)) {
            return RubyModuleCachesManager.getInstance(module);
//        }
//        return null;
    }

    /**
     * Search classes with specified name in search scope
     *
     * @param simpleName Class simpleName(not qualified)
     * @param scope      Search scope. If is null all scope will be used.
     * @param project    Project
     * @return Array with ruby classes
     */
    @NotNull
    public static RClass[] getClassesByName(@NotNull final String simpleName,
                                            @Nullable final SearchScope scope,
                                            @NotNull final Project project) {
        return getClassessByName(simpleName, scope, project, false);
    }

    /**
     * Search first class with specified name in search scope
     *
     * @param name    Class name(not qualified)
     * @param scope   Search scope. If is null all scope will be used.
     * @param project Project
     * @return null if nothing was found
     */
    @Nullable
    public static RClass getFirstClassByName(@NotNull final String name,
                                             @Nullable final SearchScope scope,
                                             @NotNull final Project project) {
        final RClass[] classes = getClassessByName(name, scope, project, true);
        return classes.length != 0 ? classes[0] : null;
    }

    /**
     * Searched class with specified name from specified file at first in modules, then in sdk
     *
     * @param className  Class name (not qualified)
     * @param project    Project
     * @param sScope     Search scope. If is null all scope will be used.
     * @param scriptFile Ruby script
     * @return Cached ruby class or null
     */
    @Nullable
    public static RVirtualClass getFirstClassByNameInScript(@NotNull final String className,
                                                            @NotNull final Project project,
                                                            @Nullable final SearchScope sScope,
                                                            @NotNull final VirtualFile scriptFile) {
        final SearchScope scope = (sScope != null ? sScope : SearchScopeUtil.getAllSearchScope());
        final DeclarationsIndex[] indexes = getDeclarationsIndexByFile(scriptFile, scope, project);
        for (DeclarationsIndex index : indexes) {
            if (index == null) {
                continue;
            }

            final List<RVirtualClass> classes = index.getClassesByName(className);
            for (RVirtualClass rClass : classes) {
                if (rClass.getVirtualFile() == scriptFile) {
                    return rClass;
                }
            }
        }
        return null;
    }

    /**
     * Searched test class with specified qualified name from specified file at first in modules, then in sdk
     *
     * @param qualifiedClassName Class name
     * @param project            Project
     * @param sScope             Search scope. If is null all scope will be used.
     * @param scriptFile         Ruby script
     * @param fSWrapper          if null nothing will happen. If wrapper contains
     *                           not null value, this value will be used for comparing qualified names, otherwise method
     *                           will store evaluated light mode symbol.
     * @return Cached ruby class or null
     */
    @Nullable
    public static RVirtualClass getClassByNameInScriptInRubyTestMode(@NotNull final String qualifiedClassName,
                                                                     @NotNull final Project project,
                                                                     @Nullable final SearchScope sScope,
                                                                     @NotNull final VirtualFile scriptFile,
                                                                     @Nullable final Ref<FileSymbol> fSWrapper) {
        final SearchScope scope = (sScope != null ? sScope : SearchScopeUtil.getAllSearchScope());
        final DeclarationsIndex[] indexes = getDeclarationsIndexByFile(scriptFile, scope, project);
        for (DeclarationsIndex index : indexes) {
            if (index == null) {
                continue;
            }

            final String realClassName = RClassPresentationUtil.getNameByQualifiedName(qualifiedClassName);
            final List<RVirtualClass> classes = index.getClassesByName(realClassName);
            for (RVirtualClass rClass : classes) {
                if (rClass.getVirtualFile() == scriptFile) {
                    // one file often contains few classes with equal names
                    final String qName =
                            RClassPresentationUtil.getRuntimeQualifiedNameInRubyTestMode(rClass, fSWrapper);
                    if (!qualifiedClassName.equals(qName)) {
                        continue;
                    }
                    return rClass;
                }
            }
        }
        return null;
    }

    @Nullable
    public static Module getModuleByFile(@NotNull final VirtualFile file,
                                         @NotNull final Project project) {
        return ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(file);
    }

    private static <T extends RVirtualElement> List<T> getItems(@NotNull final List<RVirtualClass> elements,
                                                                @NotNull final Project project,
                                                                @Nullable final SearchScope sScope) {
        final ArrayList<T> items = new ArrayList<T>();
        for (RVirtualElement prototype : elements) {
            if (sScope != null && prototype instanceof RVirtualContainer) {
                final RFileInfo info = ((RVirtualContainer) prototype).getContainingFileInfo();
                if (info != null) {
                    final String url = info.getUrl();
                    if (!sScope.isFileValid(url)) {
                        continue;
                    }
                }
            }
            final RPsiElement psiElement =
                    RVirtualPsiUtil.findPsiByVirtualElement(prototype, project);
            //noinspection unchecked
            items.add((T) psiElement);
        }
        return items;
    }

    @NotNull
    private static RClass[] getClassessByName(@NotNull final String className,
                                              @Nullable final SearchScope sScope,
                                              @NotNull final Project project,
                                              final boolean firstOnly) {
        final SearchScope scope = (sScope != null ? sScope : SearchScopeUtil.getAllSearchScope());
        final Module[] modules = RModuleUtil.getAllModulesWithRubySupport(project);
        final List<RVirtualElement> items = new ArrayList<RVirtualElement>();
        final RubySdkCachesManager sdkCachesManager = project.getComponent(RubySdkCachesManager.class);

        for (Module module : modules) {
            if (!scope.isSearchInModuleContent(module)) {
                continue;
            }
            final RubyModuleCachesManager cachesManager = getCachesManager(module);

            // CachesManager is null for not ruby modules
            if (cachesManager != null) {
                final DeclarationsIndex declarationsIndex = cachesManager.getDeclarationsIndex();
                final List<RVirtualClass> classes =
                        declarationsIndex.getClassesByName(className);
                items.addAll(getItems(classes, project, sScope));
                if (firstOnly) {
                    break;
                }
            }

// Adding sdk`s info if needed
            if (scope.isSearchInSDKLibraries()) {
                final ProjectJdk sdk = RModuleUtil.getModuleOrJRubyFacetSdk(module);
                final DeclarationsIndex declarationsIndex = sdkCachesManager.getSdkDeclarationsIndex(sdk);
                if (declarationsIndex != null) {
                    final List<RVirtualClass> classes =
                            declarationsIndex.getClassesByName(className);
                    items.addAll(getItems(classes, project, sScope));
                    if (firstOnly) {
                        break;
                    }
                }
            }
        }
        //noinspection SuspiciousToArrayCall
        return items.toArray(new RClass[items.size()]);
    }
}
