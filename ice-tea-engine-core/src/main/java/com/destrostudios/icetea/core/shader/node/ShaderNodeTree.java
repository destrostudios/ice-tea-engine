package com.destrostudios.icetea.core.shader.node;

import com.destrostudios.icetea.core.shader.ShaderHookPosition;
import com.destrostudios.icetea.core.shader.ShaderType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderNodeTree {

    private static final Pattern OUTPUT_PATTERN = Pattern.compile("(.+) (.+);(?: // @length ([0-9])+)?");
    private static final String HOOK_NAME_BASE = "base";
    private static final String HOOK_NAME_INPUTS = "inputs";
    private static final String HOOK_NAME_OUTPUTS = "outputs";

    public ShaderNodeTree(ShaderType shaderType, ArrayList<ShaderNodeOutput> inputs, ShaderNodeManager shaderNodeManager) {
        this.shaderType = shaderType;
        this.inputs = inputs;
        this.shaderNodeManager = shaderNodeManager;
        hooks = new HashMap<>();
        outputs = new ArrayList<>();
    }
    private ShaderType shaderType;
    private ArrayList<ShaderNodeOutput> inputs;
    private ShaderNodeManager shaderNodeManager;
    private ShaderNodeSnippet baseSnippet;
    private HashMap<String, HashMap<ShaderHookPosition, ArrayList<ShaderNodeSnippet>>> hooks;
    @Getter
    private ArrayList<ShaderNodeOutput> outputs;

    public boolean tryRead(ArrayList<String> nodeNames) {
        readBaseSnippet();
        readAndAddNodes(nodeNames);
        return hooks.size() > 0;
    }

    private void readBaseSnippet() {
        String path = "com/destrostudios/icetea/core/shaders/nodes/" + HOOK_NAME_BASE + ".glsl";
        baseSnippet = shaderNodeManager.readSnippet(HOOK_NAME_BASE, ShaderHookPosition.SELF, path);
    }

    private void readAndAddNodes(ArrayList<String> nodeNames) {
        ShaderNode shaderNode = shaderNodeManager.readNode(nodeNames, shaderType);
        for (ShaderNodeSnippet snippet : shaderNode.getSnippets()) {
            addSnippet(snippet);
        }
    }

    private void addSnippet(ShaderNodeSnippet snippet) {
        hooks.computeIfAbsent(snippet.getHookedHookName(), _ -> new HashMap<>()).computeIfAbsent(snippet.getHookedHookPosition(), _ -> new ArrayList<>()).add(snippet);
    }

    public String getCode() {
        return getHookCode(HOOK_NAME_BASE, baseSnippet);
    }

    private String getHookCode(String hookName, ShaderNodeSnippet snippetToResolve) {
        String code = "";
        HashMap<ShaderHookPosition, ArrayList<ShaderNodeSnippet>> hookedSnippetsByPosition = hooks.get(hookName);
        if (hookedSnippetsByPosition != null) {
            code += getHookedCode(hookedSnippetsByPosition, ShaderHookPosition.BEFORE);
        }
        if (snippetToResolve != null) {
            code += getResolvedSnippetCode(snippetToResolve);
        }
        if (hookedSnippetsByPosition != null) {
            code += getHookedCode(hookedSnippetsByPosition, ShaderHookPosition.SELF);
            code += getHookedCode(hookedSnippetsByPosition, ShaderHookPosition.END);
            code += getHookedCode(hookedSnippetsByPosition, ShaderHookPosition.AFTER);
        }
        if (hookName.equals(HOOK_NAME_INPUTS)) {
            code += getOutputsCode(inputs, "in");
        } else if (hookName.equals(HOOK_NAME_OUTPUTS)) {
            code += getOutputsCode(outputs, "out");
        }
        return code;
    }

    private String getHookedCode(HashMap<ShaderHookPosition, ArrayList<ShaderNodeSnippet>> hookedSnippetsByPosition, ShaderHookPosition hookPosition) {
        String code = "";
        ArrayList<ShaderNodeSnippet> snippets = hookedSnippetsByPosition.get(hookPosition);
        if (snippets != null) {
            for (ShaderNodeSnippet snippet : snippets) {
                code += getResolvedSnippetCode(snippet);
            }
        }
        return code;
    }

    private String getResolvedSnippetCode(ShaderNodeSnippet snippet) {
        String code = snippet.getText();
        for (String hookName : snippet.getOwnHooks()) {
            code = code.replaceAll("// @hook " + hookName, getHookCode(hookName, null)) + "\n";
        }
        if (snippet.getHookedHookName().equals(HOOK_NAME_OUTPUTS)) {
            String[] lines = code.split("\n");
            code = "";
            for (String line : lines) {
                Matcher outputMatcher = OUTPUT_PATTERN.matcher(line);
                if (outputMatcher.find()) {
                    String type = outputMatcher.group(1);
                    String name = outputMatcher.group(2);
                    String lengthText = outputMatcher.group(3);
                    int length = (lengthText != null) ? Integer.parseInt(lengthText) : 1;
                    outputs.add(new ShaderNodeOutput(type, name, length));
                } else {
                    code += line + "\n";
                }
            }
        }
        return code;
    }

    private static String getOutputsCode(ArrayList<ShaderNodeOutput> outputs, String direction) {
        String code = "";
        int location = 0;
        for (ShaderNodeOutput output : outputs) {
            String name = direction + output.getName().substring(0, 1).toUpperCase() + output.getName().substring(1);
            code += "layout(location = " + location + ") " + direction + " " + output.getType() + " " + name + ";\n";
            location += output.getLength();
        }
        return code;
    }
}
