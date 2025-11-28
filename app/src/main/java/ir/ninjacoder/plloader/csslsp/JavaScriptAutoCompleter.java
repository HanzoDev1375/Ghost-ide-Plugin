package ir.ninjacoder.plloader.csslsp;

import android.content.Context;
import android.util.Log;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.rosemoe.sora.data.CompletionItem;

public class JavaScriptAutoCompleter {

    private static final String TAG = "JavaScriptAutoCompleter";
    private org.mozilla.javascript.Context rhinoContext;
    private Scriptable scope;
    private Function completeFunction;
    
    private final Map<String, CacheEntry> completionCache = new HashMap<>();
    private static final long CACHE_DURATION = 300000;

    public JavaScriptAutoCompleter(Context androidContext) {
        initRhino(androidContext);
    }

    private void initRhino(Context androidContext) {
        try {
            rhinoContext = org.mozilla.javascript.Context.enter();
            rhinoContext.setOptimizationLevel(-1);
            scope = rhinoContext.initStandardObjects();
            loadLspLibrary(androidContext);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Rhino", e);
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }

    private void loadLspLibrary(Context androidContext) throws IOException {
        File lspFile = new File(androidContext.getFilesDir(), "jslsp.js");
        if (!lspFile.exists()) {
            createDefaultLspScript(androidContext);
        }

        StringBuilder scriptContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(lspFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                scriptContent.append(line).append("\n");
            }
        }

        org.mozilla.javascript.Context.enter();
        try {
            // اضافه کردن objectهای جاوا به JavaScript
            ScriptableObject.putProperty(scope, "javaBridge", new JavaBridge());
            
            rhinoContext.evaluateString(scope, scriptContent.toString(), "jslsp.js", 1, null);
            Object completeObj = scope.get("complete", scope);
            if (completeObj instanceof Function) {
                completeFunction = (Function) completeObj;
                Log.d(TAG, "Complete function loaded successfully");
            } else {
                Log.e(TAG, "Complete function not found in JavaScript");
            }
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }

    private void createDefaultLspScript(Context androidContext) throws IOException {
        String defaultLsp = """
            // Bridge برای ارتباط با جاوا
            var JavaBridge = javaBridge;
            
            // لیست کامل keywordهای جاوااسکریپت
            var javascriptKeywords = [
                'function', 'var', 'let', 'const', 'if', 'else', 'for', 'while', 'do',
                'switch', 'case', 'break', 'continue', 'return', 'try', 'catch', 'finally',
                'throw', 'new', 'this', 'class', 'extends', 'import', 'export', 'default',
                'async', 'await', 'typeof', 'instanceof', 'in', 'of', 'delete', 'void',
                'with', 'debugger', 'yield', 'static', 'super', 'implements', 'interface',
                'package', 'private', 'protected', 'public'
            ];

            // global objects و متدهای آنها
            var globalCompletions = {
                'console': ['log', 'warn', 'error', 'info', 'debug', 'table', 'time', 'timeEnd', 'trace', 'assert'],
                'Math': ['abs', 'floor', 'ceil', 'round', 'max', 'min', 'random', 'sqrt', 'pow', 'sin', 'cos', 'tan', 'PI', 'E'],
                'String': ['fromCharCode', 'fromCodePoint', 'prototype', 'length'],
                'Array': ['isArray', 'from', 'of', 'prototype'],
                'Object': ['keys', 'values', 'entries', 'assign', 'create', 'defineProperty'],
                'JSON': ['parse', 'stringify'],
                'Number': ['isNaN', 'isFinite', 'parseInt', 'parseFloat'],
                'Date': ['now', 'parse', 'UTC'],
                'Promise': ['all', 'race', 'resolve', 'reject'],
                'Map': ['prototype', 'size'],
                'Set': ['prototype', 'size'],
                'Function': ['prototype', 'length'],
                'RegExp': ['prototype'],
                'Error': ['prototype'],
                'Boolean': ['prototype'],
                'Symbol': ['for', 'keyFor']
            };

            // متدهای prototype برای انواع داده
            var prototypeMethods = {
                'String': ['charAt', 'charCodeAt', 'concat', 'includes', 'indexOf', 'lastIndexOf', 
                          'replace', 'slice', 'split', 'substring', 'toLowerCase', 'toUpperCase', 
                          'trim', 'trimStart', 'trimEnd', 'startsWith', 'endsWith', 'padStart', 'padEnd'],
                'Array': ['push', 'pop', 'shift', 'unshift', 'slice', 'splice', 'concat', 'join', 
                         'reverse', 'sort', 'filter', 'map', 'forEach', 'reduce', 'find', 'findIndex', 
                         'some', 'every', 'includes', 'indexOf', 'lastIndexOf'],
                'Object': ['toString', 'valueOf', 'hasOwnProperty', 'isPrototypeOf', 'propertyIsEnumerable'],
                'Number': ['toFixed', 'toExponential', 'toPrecision', 'toString'],
                'Function': ['call', 'apply', 'bind'],
                'Date': ['getDate', 'getDay', 'getFullYear', 'getHours', 'getMilliseconds', 
                        'getMinutes', 'getMonth', 'getSeconds', 'getTime', 'getTimezoneOffset',
                        'setDate', 'setFullYear', 'setHours', 'setMilliseconds', 'setMinutes', 
                        'setMonth', 'setSeconds', 'setTime']
            };

            function complete(code, line, column, prefix) {
                JavaBridge.log("Complete called with prefix: " + prefix);
                
                var results = [];
                var lowerPrefix = prefix.toLowerCase();
                
                try {
                    // اگر شامل . هست (member completion)
                    if (prefix.includes('.')) {
                        var parts = prefix.split('.');
                        var base = parts[0];
                        var member = parts[1] || '';
                        
                        JavaBridge.log("Base: " + base + ", Member: " + member);
                        
                        // بررسی global objects
                        if (globalCompletions[base]) {
                            globalCompletions[base].forEach(function(item) {
                                if (item.toLowerCase().includes(member.toLowerCase())) {
                                    results.push({
                                        label: item,
                                        commit: item,
                                        desc: base + '.' + item + ' - Method',
                                        kind: 'method'
                                    });
                                }
                            });
                        }
                        
                        // بررسی prototype methods
                        if (prototypeMethods[base]) {
                            prototypeMethods[base].forEach(function(item) {
                                if (item.toLowerCase().includes(member.toLowerCase())) {
                                    results.push({
                                        label: item,
                                        commit: item,
                                        desc: base + '.prototype.' + item + ' - Prototype method',
                                        kind: 'method'
                                    });
                                }
                            });
                        }
                        
                    } else {
                        // keyword completion
                        javascriptKeywords.forEach(function(keyword) {
                            if (keyword.toLowerCase().includes(lowerPrefix)) {
                                results.push({
                                    label: keyword,
                                    commit: keyword,
                                    desc: 'JavaScript keyword',
                                    kind: 'keyword'
                                });
                            }
                        });
                        
                        // global objects completion
                        Object.keys(globalCompletions).forEach(function(obj) {
                            if (obj.toLowerCase().includes(lowerPrefix)) {
                                results.push({
                                    label: obj,
                                    commit: obj,
                                    desc: 'Global object',
                                    kind: 'class'
                                });
                            }
                        });
                        
                        // prototype methods as standalone (برای وقتی که تایپ شده)
                        Object.keys(prototypeMethods).forEach(function(obj) {
                            prototypeMethods[obj].forEach(function(method) {
                                if (method.toLowerCase().includes(lowerPrefix)) {
                                    results.push({
                                        label: method,
                                        commit: method,
                                        desc: obj + ' method',
                                        kind: 'method'
                                    });
                                }
                            });
                        });
                    }
                    
                    // اگر نتیجه ای پیدا نشد، همه keywordها رو برگردون
                    if (results.length === 0 && prefix.length > 0) {
                        javascriptKeywords.forEach(function(keyword) {
                            if (keyword.toLowerCase().startsWith(lowerPrefix)) {
                                results.push({
                                    label: keyword,
                                    commit: keyword,
                                    desc: 'JavaScript keyword',
                                    kind: 'keyword'
                                });
                            }
                        });
                    }
                    
                    JavaBridge.log("Found " + results.length + " completions");
                    
                } catch (error) {
                    JavaBridge.log("Error in complete: " + error);
                }
                
                return JSON.stringify(results);
            }

            // تابع کمکی برای لاگ
            function log(message) {
                JavaBridge.log("[JS] " + message);
            }
            """;

        File lspFile = new File(androidContext.getFilesDir(), "jslsp.js");
        try (java.io.FileWriter writer = new java.io.FileWriter(lspFile)) {
            writer.write(defaultLsp);
        }
    }

    public synchronized List<CompletionItem> complete(String code, int line, int column, String prefix) {
        Log.d(TAG, "complete() called - Line: " + line + ", Column: " + column + ", Prefix: '" + prefix + "'");
        
        String cacheKey = line + ":" + column + ":" + prefix;
        
        CacheEntry cached = completionCache.get(cacheKey);
        if (cached != null && System.currentTimeMillis() - cached.timestamp < CACHE_DURATION) {
            Log.d(TAG, "Returning cached results: " + cached.items.size() + " items");
            return cached.items;
        }
        
        try {
            org.mozilla.javascript.Context.enter();
            
            if (completeFunction == null) {
                Log.e(TAG, "Complete function is null!");
                return new ArrayList<>();
            }
            
            Object[] args = { code, line, column, prefix };
            Object result = completeFunction.call(rhinoContext, scope, scope, args);
            String jsonResult = org.mozilla.javascript.Context.toString(result);
            
            Log.d(TAG, "JavaScript returned: " + jsonResult);
            
            List<CompletionItem> completions = parseCompletions(jsonResult, prefix);
            
            completionCache.put(cacheKey, new CacheEntry(completions));
            
            Log.d(TAG, "Returning " + completions.size() + " completions");
            return completions;
            
        } catch (Exception e) {
            Log.e(TAG, "Error in JavaScript completion", e);
            return new ArrayList<>();
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }

    private List<CompletionItem> parseCompletions(String jsonStr, String prefix) {
        List<CompletionItem> results = new ArrayList<>();
        
        if (jsonStr == null || jsonStr.trim().isEmpty() || jsonStr.equals("null")) {
            Log.d(TAG, "Empty JSON result from JavaScript");
            return results;
        }
        
        try {
            org.mozilla.javascript.Context.enter();
            Scriptable jsonScope = rhinoContext.initStandardObjects();
            
            // استفاده از JSON.parse در Rhino
            String script = "JSON.parse('" + jsonStr.replace("'", "\\'") + "')";
            Object parsed = rhinoContext.evaluateString(jsonScope, script, "json", 1, null);
            
            if (parsed instanceof Scriptable) {
                Scriptable array = (Scriptable) parsed;
                Object length = array.get("length", array);
                
                if (length instanceof Number) {
                    int len = ((Number) length).intValue();
                    Log.d(TAG, "Parsing " + len + " completion items");
                    
                    for (int i = 0; i < len; i++) {
                        Object item = array.get(i, array);
                        if (item instanceof Scriptable) {
                            Scriptable obj = (Scriptable) item;
                            
                            String label = getStringProperty(obj, "label");
                            String commit = getStringProperty(obj, "commit");
                            String desc = getStringProperty(obj, "desc");
                            
                            if (label != null) {
                                CompletionItem completionItem = new CompletionItem(label, commit != null ? commit : label, desc);
                                // تنظیم icon بر اساس kind
                                String kind = getStringProperty(obj, "kind");
                                if ("keyword".equals(kind)) {
                                    completionItem.desc = "Keyword: " + label;
                                } else if ("method".equals(kind)) {
                                    completionItem.desc = "Method: " + label;
                                } else if ("class".equals(kind)) {
                                    completionItem.desc = "Class: " + label;
                                }
                                
                                results.add(completionItem);
                                Log.d(TAG, "Added completion: " + label);
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing completions JSON: " + jsonStr, e);
        } finally {
            org.mozilla.javascript.Context.exit();
        }
        
        return results;
    }

    private String getStringProperty(Scriptable obj, String property) {
        Object value = obj.get(property, obj);
        return (value != null && !org.mozilla.javascript.Context.getUndefinedValue().equals(value)) 
               ? value.toString() : null;
    }

    private void cleanupOldCache() {
        long currentTime = System.currentTimeMillis();
        completionCache.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().timestamp > CACHE_DURATION
        );
    }

    // کلاس JavaBridge برای ارتباط بین جاوا و JavaScript
    public class JavaBridge {
        public void log(String message) {
            Log.d(TAG, message);
        }
    }

    private static class CacheEntry {
        long timestamp;
        List<CompletionItem> items;
        
        CacheEntry(List<CompletionItem> items) {
            this.timestamp = System.currentTimeMillis();
            this.items = items;
        }
    }

    public void destroy() {
        if (rhinoContext != null) {
            org.mozilla.javascript.Context.exit();
            rhinoContext = null;
            scope = null;
            completeFunction = null;
        }
    }
}