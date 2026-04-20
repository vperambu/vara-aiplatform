package com.vara.org.vara_copilotai.orchestration;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class ToolDispatcher {

    private final Map<String, Tool> tools = new HashMap<>();

    public ToolDispatcher() {
        // Register tools, e.g., calculator, API calls
        tools.put("calculator", new CalculatorTool());
    }

    public String invokeTool(String toolName, String input) {
        Tool tool = tools.get(toolName);
        if (tool != null) {
            return tool.execute(input);
        }
        return "Tool not found";
    }

    interface Tool {
        String execute(String input);
    }

    static class CalculatorTool implements Tool {
        @Override
        public String execute(String input) {
            // Simple calculator placeholder
            return "Calculated: " + input;
        }
    }
}
