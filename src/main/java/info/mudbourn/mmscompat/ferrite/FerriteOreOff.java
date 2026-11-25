package info.mudbourn.mmscompat.ferrite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Turns off Ferrite's ore kernel, whose direct section writes break Lithium's tick counts and leave chunks ungenerated.
public final class FerriteOreOff {

    private static final Logger LOG = LoggerFactory.getLogger("mms_compat");
    private static final String MODE_PROPERTY = "ferrite.orekernel";
    private static final String KERNEL = "me.apika.apikaprobe.worldgen.OreKernel";

    private FerriteOreOff() {
    }

    public static void register() {
        System.setProperty(MODE_PROPERTY, "off");
        try {
            Class<?> kernel = Class.forName(KERNEL);
            kernel.getField("mode").setInt(null, kernel.getField("OFF").getInt(null));
            LOG.info("Ferrite ore kernel turned off; vanilla ore placement runs instead.");
        } catch (ReflectiveOperationException e) {
            LOG.warn("Could not turn off Ferrite's ore kernel", e);
        }
    }
}
