package org.intellij.j2ee.web.resin.resin.version;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * JarResources: JarResources maps all resources included in a
 * Zip or Jar file. Additionaly, it provides a method to extract one
 * as a blob.
 *
 * From <a href="http://www.javaworld.com/javaworld/javatips/javatip70/JarResources.java">http://www.javaworld.com/javaworld/javatips/javatip70/JarResources.java</a>
 */
final class JarResources {

    // external debug flag
    public boolean debugOn = false;

    // jar resource mapping tables
    private final Map<String, byte[]> htJarContents = new Hashtable<String, byte[]>();

    // a jar file
    private String jarFileName;
    private boolean fullLoaded;

    /**
     * creates a JarResources. It extracts all resources from a Jar
     * into an internal hashtable, keyed by resource names.
     *
     * @param jarFileName a jar or zip file
     */
    public JarResources(String jarFileName) {
        this(jarFileName, true);
    }

    /**
     * creates a JarResources. It extracts all resources from a Jar
     * into an internal hashtable, keyed by resource names.
     *
     * @param jarFileName a jar or zip file
     * @param loadAll flag to load on startup all Jar classes. true yes, false on demand
     */
    public JarResources(String jarFileName, boolean loadAll) {
        this.jarFileName = jarFileName;
        this.fullLoaded = loadAll;
        if(loadAll)
            fullScan();
    }

    /**
     * Extracts a jar resource as a blob.
     *
     * @param name a resource name.
     * @return resource content
     */
    public byte[] getResource(String name) {
        if(!fullLoaded && !htJarContents.containsKey(name))
            loadOnDemand(name);

        return htJarContents.get(name);
    }

    /**
     * initializes internal hash tables with Jar file resources.
     */
    private void fullScan() {
        try {
            Map<String, Integer> htSizes = new Hashtable<String, Integer>();
            
            // extracts just sizes only.
            ZipFile zf = new ZipFile(jarFileName);
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                htSizes.put(ze.getName(), (int) ze.getSize());
            }
            zf.close();

            // extract resources and put them into the hashtable.
            FileInputStream fis = new FileInputStream(jarFileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }

                if (debugOn) {
                    System.out.println("ze.getName()=" + ze.getName() +
                            "," + "getSize()=" + ze.getSize());
                }

                int size = (int) ze.getSize();
                // -1 means unknown size.
                if (size == -1) {
                    size = htSizes.get(ze.getName());
                }

                byte[] b = new byte[size];
                int rb = 0;
                int chunk;
                while ((size - rb) > 0) {
                    chunk = zis.read(b, rb, size - rb);
                    if (chunk == -1) {
                        break;
                    }
                    rb += chunk;
                }

                // add to internal resource hashtable
                htJarContents.put(ze.getName(), b);

                if (debugOn) {
                    System.out.println(ze.getName() + "  rb=" + rb +
                            ",size=" + size +
                            ",csize=" + ze.getCompressedSize());
                }
            }
        }
        catch (NullPointerException e) {
            System.out.println("done.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a resource on demand
     * @param name the resource name
     */
    private void loadOnDemand(String name) {
        ZipFile zf = null;
        InputStream is = null;
        try {
            zf = new ZipFile(jarFileName);
            ZipEntry ze = zf.getEntry(name);
            if(ze == null)
                return;

            is = zf.getInputStream(ze);
            //TODO improve this to read big resources (not needed yet)
            int size = is.available();
            if(size == -1)
                return;

            byte[] b = new byte[size];
            int readed = is.read(b);

            // add to internal resource hashtable
            htJarContents.put(ze.getName(), b);
            is.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(zf != null)
                try {
                    zf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if(is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

}    // End of JarResources class.
