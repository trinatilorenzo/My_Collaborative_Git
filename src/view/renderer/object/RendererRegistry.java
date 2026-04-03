package view.renderer.object;

import model.object.GameObject;
import java.util.HashMap;
import java.util.Map;

/**
 * The OBJECT RENDERER REGISTRY CLASS manages the association between game object types and their corresponding renderers. It allows for dynamic registration and retrieval of renderers based on the class of the game object, facilitating a flexible rendering system that can easily accommodate new object types and their visual representations.
 */
public class RendererRegistry {
    private Map<Class<? extends GameObject>, ObjectRender<? extends GameObject>> registry;
    
    public RendererRegistry() {
        this.registry = new HashMap<>();
    }

    public <T extends GameObject> void register(Class<T> objClass, ObjectRender<? super T> renderer) {
        registry.put(objClass, renderer);
    }

    @SuppressWarnings("unchecked")
    public <T extends GameObject> ObjectRender<T> getRenderer(Class<T> objClass) {
        return (ObjectRender<T>) registry.get(objClass);
    }

}
