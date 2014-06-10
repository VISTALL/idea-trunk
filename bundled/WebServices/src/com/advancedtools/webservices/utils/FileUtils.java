package com.advancedtools.webservices.utils;

import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.intellij.compiler.CompilerConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Maxim
 * @author Konstantin Bulenkov
 */
public class FileUtils {
  @NonNls
  public static final String CLASS_RESOURCE_STRING = "*?.class";
  @NonNls
  private static final String SOAP_ADDRESS = "soap:address";

  public static File saveStreamContentAsFile(String fullFileName, InputStream stream) throws IOException {
    fullFileName = findFreeFileName(fullFileName);
    OutputStream ostream = new FileOutputStream(fullFileName);
    byte[] buf = new byte[8192];

    while(true) {
      int read = stream.read(buf,0,buf.length);
      if (read == -1) break;
      ostream.write(buf,0,read);
    }
    ostream.flush();
    ostream.close();
    return new File(fullFileName);
  }

  private static String findFreeFileName(String filename) {
    File f = new File(filename);
    if (! f.exists()) return filename;
    int dot = filename.lastIndexOf('.'); // we believe file has some ext. For instance,  ".wsdl"
    String name = filename.substring(0, dot);
    String ext = filename.substring(dot);
    int num = 0;
    do {
      f = new File(name + ++num + ext);
    } while (f.exists());
    return name + num + ext;
  }

  public static File createTempDir(String prefix) throws IOException {
    File tempDir = File.createTempFile(prefix,"");
    tempDir.delete();
    tempDir.mkdir();
    EnvironmentFacade.deleteDirectoryOnExit(tempDir);
    return tempDir;
  }

  public static VirtualFile findWebXml(Module currentModule) {
    ModuleFileIndex fileIndex = ModuleRootManager.getInstance(currentModule).getFileIndex();
    final VirtualFile[] result = new VirtualFile[1];

    fileIndex.iterateContent(new ContentIterator() {
      public boolean processFile(VirtualFile virtualFile) {
        if (virtualFile.getName().equals("web.xml")) {
          result[0] = virtualFile;
          return false;
        }
        return true;
      }
    });
    return result[0];
  }

  public static void addClassAsCompilerResource(Project project) {
    if (!CompilerConfiguration.getInstance(project).isResourceFile("A.class")) {
      try {
        EnvironmentFacade.getInstance().addCompilerResourcePattern(project, CLASS_RESOURCE_STRING);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void saveText(VirtualFile virtualFile, String text) throws IOException {
    OutputStream outputStream = virtualFile.getOutputStream(virtualFile);
    FileUtil.copy(new ByteArrayInputStream(text.getBytes(virtualFile.getCharset().name())), outputStream);
    outputStream.close();
  }

  public static void copyWsdlWithReplacementOfSoapAddress(final File generatedFileName, File _file, final @NonNls String webServiceUrl) throws IOException {
    copyWsdlWithReplacementOfSoapAddress(generatedFileName, _file, "REPLACE_WITH_ACTUAL_URL", webServiceUrl);
  }

  public static void copyWsdlWithReplacementOfSoapAddress(final File generatedFileName, File _file,
                                                        final @NonNls String existingPattern,
                                                        final @NonNls String replacementUrl
                                                        ) throws IOException {
    final boolean generatedAndDestinationFileAreTheSame = _file.equals(generatedFileName);
    final File file = generatedAndDestinationFileAreTheSame ? File.createTempFile(generatedFileName.getName(), ".tmp") : _file;

    LibUtils.scanFile(generatedFileName, new LibUtils.FileProcessor() {
      final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

      final String ls = System.getProperty("line.separator");

      public void fileScanningEnded() throws IOException {
        out.close();
        if (generatedAndDestinationFileAreTheSame) {
          generatedFileName.delete();
          file.renameTo(generatedFileName);
        }
      }

      public boolean process(String s) throws IOException {
        final int soapAddressIndex = s.indexOf(SOAP_ADDRESS);

        if (soapAddressIndex != -1) {
          String tagPrefix = s.substring(s.lastIndexOf('<',soapAddressIndex)+1, soapAddressIndex);
          final Pattern p = Pattern.compile("(.*)(<" + tagPrefix + "soap:address location=\"" + existingPattern + "\"/>)(.*)");
          final Matcher matcher = p.matcher(s);
          if (matcher.matches()) {
            s = matcher.group(1) + "<" + tagPrefix + "soap:address location=\""+replacementUrl+ "\"/>"+matcher.group(3);
          }
        }
        out.write((s + ls).getBytes());
        return true;
      }
    });
  }

  public static String removeFileProtocolPrefixIfPresent(String u) {
    if (u.startsWith(LibUtils.FILE_URL_PREFIX)) {
      u = u.substring(LibUtils.FILE_URL_PREFIX.length());
    }
    return u;
  }

  public static boolean copyFile(File in, File out) {
    try {
      FileInputStream fis = new FileInputStream(in);
      FileOutputStream fos = new FileOutputStream(out);
      byte[] buf = new byte[1024];
      int i;
      while ((i = fis.read(buf)) != -1) {
        fos.write(buf, 0, i);
      }
      fis.close();
      fos.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  @Nullable
  public static VirtualFile findWebInf(Module currentModule) {
    ModuleFileIndex fileIndex = ModuleRootManager.getInstance(currentModule).getFileIndex();
    final VirtualFile[] result = new VirtualFile[1];

    fileIndex.iterateContent(new ContentIterator() {
      public boolean processFile(VirtualFile virtualFile) {
        if (virtualFile.getName().equals("WEB-INF") && virtualFile.isDirectory()) {
          result[0] = virtualFile;
          return false;
        }
        return true;
      }
    });
    return result[0];
  }
}
