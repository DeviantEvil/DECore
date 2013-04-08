package net.deviantevil.core;

public interface CoreModule {
    public static final String PERMISSION_ROOT = "decore";
    
    String getName();

    void enable();
    void disable();
    
    public Class<?> getDatabaseClass();
}
