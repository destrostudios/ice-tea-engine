package com.destrostudios.icetea.core.shader.node;

import com.destrostudios.icetea.core.asset.AssetManager;
import com.destrostudios.icetea.core.asset.AssetNotFoundException;
import com.destrostudios.icetea.core.shader.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;

public class ShaderNodeManager {

    // Having one shared list for both compute+graphics here is fine as they don't interact with each other
    private static ShaderType[] ORDERED_SHADER_TYPES = new ShaderType[] {
        // Compute
        ShaderType.COMPUTE_SHADER,
        // Graphics
        ShaderType.VERTEX_SHADER,
        ShaderType.TESSELLATION_CONTROL_SHADER,
        ShaderType.TESSELLATION_EVALUATION_SHADER,
        ShaderType.GEOMETRY_SHADER,
        ShaderType.FRAGMENT_SHADER,
    };

    public ShaderNodeManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        rootPaths = new ArrayList<>();
        nodeCache = new HashMap<>();
        shaderCache = new HashMap<>();
    }
    private AssetManager assetManager;
    private ArrayList<String> rootPaths;
    private HashMap<ShaderType, HashMap<String, ShaderNode>> nodeCache;
    private HashMap<String, HashMap<ShaderType, Shader>> shaderCache;

    public void addRootPath(String rootPath) {
        rootPaths.add(rootPath);
    }

    public void removeRootPath(String rootPath) {
        rootPaths.remove(rootPath);
    }

    public HashMap<ShaderType, Shader> getShaders(ArrayList<String> nodeNames) {
        return shaderCache.computeIfAbsent(getKey(nodeNames), _ -> readShaders(nodeNames));
    }

    private HashMap<ShaderType, Shader> readShaders(ArrayList<String> nodeNames) {
        HashMap<ShaderType, Shader> shaders = new HashMap<>();
        ArrayList<ShaderNodeOutput> nextInputs = new ArrayList<>();
        for (ShaderType shaderType : ORDERED_SHADER_TYPES) {
            ShaderNodeTree tree = new ShaderNodeTree(shaderType, nextInputs, this);
            if (tree.tryRead(nodeNames)) {
                shaders.put(shaderType, new NodeShader(nodeNames, tree.getCode()));
                nextInputs = tree.getOutputs();
            }
        }
        return shaders;
    }

    public ShaderNode readNode(ArrayList<String> nodeNames, ShaderType shaderType) {
        HashMap<String, ShaderNode> cache = nodeCache.computeIfAbsent(shaderType, _ -> new HashMap<>());
        String key = getKey(nodeNames);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        HashSet<ShaderNodeSnippet> snippets = new HashSet<>();
        for (String nodeName : nodeNames) {
            for (String rootPath : rootPaths) {
                try {
                    String json = assetManager.loadString(rootPath + nodeName + "/node.json");
                    JSONObject root = new JSONObject(json);
                    JSONArray dependencies = root.getJSONArray("dependencies");
                    addDependencies(dependencies, rootPath, nodeName, shaderType, snippets);
                    break;
                } catch (AssetNotFoundException ex) {
                    // Ignore and try next root
                }
            }
        }
        ShaderNode node = new ShaderNode(snippets);
        cache.put(key, node);
        return node;
    }

    private void addDependencies(JSONArray dependencies, String rootPath, String nodeName, ShaderType shaderType, HashSet<ShaderNodeSnippet> snippets) {
        // TODO: Introduce TempVars
        ArrayList<String> tmpDependencyNames = new ArrayList<>();
        for (int i = 0; i < dependencies.length(); i++) {
            String dependencyName = dependencies.getString(i);

            tmpDependencyNames.clear();
            tmpDependencyNames.add(dependencyName);
            ShaderNode dependencyNode = readNode(tmpDependencyNames, shaderType);
            snippets.addAll(dependencyNode.getSnippets());

            for (ShaderHookPosition hookPosition : ShaderHookPosition.values()) {
                String snippetPath = getShaderSnippetPath(rootPath, nodeName, dependencyName, hookPosition, shaderType);
                ShaderNodeSnippet snippet = readSnippet(dependencyName, hookPosition, snippetPath);
                if (snippet != null) {
                    snippets.add(snippet);
                }
            }
        }
    }

    private static String getShaderSnippetPath(String rootPath, String nodeName, String hookName, ShaderHookPosition hookPosition, ShaderType shaderType) {
        String path = rootPath + nodeName + "/" + hookName;
        if (hookPosition.getExtension().length() > 0) {
            path += "." + hookPosition.getExtension();
        }
        path += "." + shaderType.getExtension();
        return path;
    }

    public ShaderNodeSnippet readSnippet(String hookedHookName, ShaderHookPosition hookedHookPosition, String path) {
        try {
            String text = assetManager.loadString(path);
            HashSet<String> ownHooks = parseOwnHooks(text);
            return new ShaderNodeSnippet(hookedHookName, hookedHookPosition, text, ownHooks);
        } catch (AssetNotFoundException ex) {
            return null;
        }
    }

    private static HashSet<String> parseOwnHooks(String text) {
        HashSet<String> hooks = new HashSet<>();
        for (String line : text.split("\n")) {
            Matcher matcher = ShaderManager.HOOK_PATTERN.matcher(line);
            if (matcher.find()) {
                String hookName = matcher.group(1);
                hooks.add(hookName);
            }
        }
        return hooks;
    }

    private static String getKey(ArrayList<String> nodeNames) {
        String key = "";
        for (String nodeName : nodeNames) {
            key += nodeName + "_";
        }
        return key;
    }

    public void clear() {
        nodeCache.clear();
        shaderCache.clear();
    }
}
