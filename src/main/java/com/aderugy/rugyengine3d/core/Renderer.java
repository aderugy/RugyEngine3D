package com.aderugy.rugyengine3d.core;

import com.aderugy.rugyengine3d.core.gameobjects.Camera;
import com.aderugy.rugyengine3d.core.gameobjects.components.Material;
import com.aderugy.rugyengine3d.core.gameobjects.components.materials.ColorMaterial;
import com.aderugy.rugyengine3d.core.gameobjects.components.materials.TextureMaterial;
import com.aderugy.rugyengine3d.core.gameobjects.fragmentdata.Vertex;
import com.aderugy.rugyengine3d.core.gameobjects.fragmentdata.VertexData;
import com.aderugy.rugyengine3d.core.gameobjects.primitives.Cube;
import com.aderugy.rugyengine3d.core.gameobjects.primitives.Primitives;
import com.aderugy.rugyengine3d.core.gameobjects.primitives.Rectangle;
import com.aderugy.rugyengine3d.core.gameobjects.primitives.Triangle;
import com.aderugy.rugyengine3d.core.gameobjects.shaders.Shader;
import com.aderugy.rugyengine3d.core.utils.ShaderManager;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Renderer {
    private long window;

    private Scene scene;

    public Renderer() {
        this.scene = new Scene();
    }

    /**
     * Called in the loop that renders the window.
     * Responsible for the rendering of the next frame.
     */
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        scene.drawComponents();
    }

    /**
     * Called before starting rendering the window.
     * Shader compilation and linking, using program and deleting the shaders in the memory.
     */
    public void initRenderer() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

        // --------------------------------------------------------------- TESTS
        Shader shader = ShaderManager.createShaderProgram("texture");
        Material material = new TextureMaterial("sprite.png");

        Camera camera = scene.getCamera();

        final float cameraSpeed = 0.1f;

        InputHandler.getInstance().addOnKeyPressedEventListener(GLFW_KEY_W, () -> {
            Vector3f result = new Vector3f();
            camera.getCameraFront().mul(cameraSpeed, result);
            camera.getCameraPosition().add(result);
        });

        InputHandler.getInstance().addOnKeyPressedEventListener(GLFW_KEY_S, () -> {
            Vector3f result = new Vector3f();
            camera.getCameraFront().mul(cameraSpeed, result);
            camera.getCameraPosition().sub(result);
        });

        InputHandler.getInstance().addOnKeyPressedEventListener(GLFW_KEY_A, () -> {
            Vector3f result = new Vector3f();
            camera.getCameraFront().cross(camera.getCameraUp(), result);
            result.normalize();
            result.mul(cameraSpeed);
            camera.getCameraPosition().sub(result);
        });

        InputHandler.getInstance().addOnKeyPressedEventListener(GLFW_KEY_D, () -> {
            Vector3f result = new Vector3f();
            camera.getCameraFront().cross(camera.getCameraUp(), result);
            result.normalize();
            result.mul(cameraSpeed);
            camera.getCameraPosition().add(result);
        });

        InputHandler.getInstance().addOnKeyPressedEventListener(GLFW_KEY_SPACE, () -> {
            Vector3f result = new Vector3f();
            camera.getCameraUp().mul(cameraSpeed, result);
            camera.getCameraPosition().add(result);
        });

        InputHandler.getInstance().addOnKeyPressedEventListener(GLFW_KEY_LEFT_SHIFT, () -> {
            Vector3f result = new Vector3f();
            camera.getCameraUp().mul(cameraSpeed, result);
            camera.getCameraPosition().sub(result);
        });

        InputHandler.getInstance().setMouseInput(camera, 0.1f);
        InputHandler.getInstance().setCursorVisibility(GLFW_CURSOR_DISABLED);

        Cube triangle = Primitives.cube(shader, material, 1, -0.5f, -0.5f, -0.5f);
        scene.addComponent(triangle);
        // --------------------------------------------------------------- TESTS
    }

    /**
     * Starting the application and freeing memory when closing
     */
    public void run() {
        System.out.println("Starting LWJGL " + Version.getVersion());

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * LWJGL Initialization.
     * Setting up the window and the callbacks
     */
    public void init() {
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(800, 600, "TEST PHASE", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Set up a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // Flipping the open gl images on load (so they are not inverted when rendering)
        stbi_set_flip_vertically_on_load(true);

        // Creating the InputHandler
        InputHandler.init(window);
    }

    /**
     * The loop that updates the window
     */
    public void loop() {
        // Create capabilities, clear the color and initialize shaders
        this.initRenderer();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            // Rendering
            this.render();
            glfwSwapBuffers(window);

            // Process inputs
            InputHandler.getInstance().processOnKeyPressedEvents();

            // Calling events that happened during the last iteration
            glfwPollEvents();
        }
    }
}
