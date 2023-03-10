package com.aderugy.rugyengine3d.core.utils;

import java.io.File;
import java.net.URL;

/**
 * Provides the resources to the other classes
 */
public class ResourceManager {
    /**
     * Returns the associated shader (in resources/shaders)
     * @param filename name of the shader file
     * @return File corresponding to the associated shader
     */
    public static File getShader(String filename, String ext) {
        return getResource("shaders/" + filename + "/" + filename + ext);
    }

    /**
     * Returns the associated image (in resources/image)
     * @param filename name of the image
     * @return File corresponding to the associated image
     */
    public static File getImage(String filename) {
        return getResource("images/" + filename);
    }

    /**
     * @throws RuntimeException file not found
     * @param path path to the seeked resource.
     * @return File corresponding to the seeked resource.
     */
    private static File getResource(String path) {
        URL url = ResourceManager.class.getClassLoader().getResource(path);
        if (url == null)
            throw new RuntimeException("No such file: '" + path + "'");
        return new File (url.getPath());
    }
}
