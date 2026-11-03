package info.mudbourn.mmscompat.mixin;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import info.mudbourn.mmscompat.client.ChatEmoji;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Opens a Discord-style emoji picker in chat while a {@code :shortcode} is being
 * typed, listing matching shortcodes with their emoji.
 */
@Mixin(CommandSuggestions.class)
public abstract class ChatEmojiSuggestionsMixin {

    @Unique
    private static final Pattern MMS$PARTIAL = Pattern.compile("(?:^|\\s):([a-zA-Z0-9_+\\-]{2,})$");

    @Shadow
    @Final
    EditBox input;

    @Shadow
    @Final
    private boolean commandsOnly;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    public abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Inject(method = "updateCommandInfo", at = @At("TAIL"))
    private void mms$suggestEmoji(CallbackInfo ci) {
        String text = input.getValue();
        if (commandsOnly || text.startsWith("/")) {
            return;
        }
        String typed = text.substring(0, input.getCursorPosition());
        Matcher m = MMS$PARTIAL.matcher(typed);
        if (!m.find()) {
            return;
        }
        SuggestionsBuilder builder = new SuggestionsBuilder(typed, m.start(1) - 1);
        for (Map.Entry<String, String> e : ChatEmoji.matching(m.group(1), 40)) {
            Message tooltip = Component.literal(e.getValue());
            builder.suggest(":" + e.getKey() + ":", tooltip);
        }
        pendingSuggestions = builder.buildFuture();
        pendingSuggestions.thenRun(() -> showSuggestions(false));
    }
}
