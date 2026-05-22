package ci.aiplatform.chat.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

/**
 * Registre minimal des outils. Chaque tool vérifie le scope du token avant exécution.
 * Implémentations détaillées (sandbox Docker, Git, SQL) à brancher en production.
 */
@Slf4j
@Component
public class ToolRegistry {

    public Object execute(String tool, Map<String, Object> args, String userId, java.util.Set<String> scopes) {
        log.info("tool_call user={} tool={} args={}", userId, tool, args.keySet());
        return switch (tool) {
            case "calculator" -> calculator(args);
            case "code_executor" -> mustHave(scopes, "CODE_EXECUTE", () -> "code execution sandbox not enabled in dev");
            case "sql_query" -> mustHave(scopes, "DATA_INGEST", () -> "sql tool stub");
            case "git_tool" -> mustHave(scopes, "AGENT_EXECUTE", () -> "git tool stub");
            case "file_reader" -> mustHave(scopes, "KNOWLEDGE_READ", () -> "file reader stub");
            case "web_search" -> mustHave(scopes, "AGENT_EXECUTE", () -> "searxng stub");
            case "payroll_tool" -> mustHave(scopes, "PAYROLL_READ", () -> "payroll calculator stub");
            case "document_gen" -> mustHave(scopes, "AGENT_EXECUTE", () -> "doc gen stub");
            default -> Map.of("error", "unknown tool: " + tool);
        };
    }

    private Object mustHave(java.util.Set<String> scopes, String required, java.util.function.Supplier<Object> fn) {
        if (scopes != null && !scopes.contains(required)) {
            return Map.of("error", "missing scope: " + required);
        }
        return fn.get();
    }

    private Object calculator(Map<String, Object> args) {
        try {
            BigDecimal a = new BigDecimal(args.get("a").toString());
            BigDecimal b = new BigDecimal(args.get("b").toString());
            String op = args.getOrDefault("op", "add").toString();
            BigDecimal r = switch (op) {
                case "add" -> a.add(b);
                case "sub" -> a.subtract(b);
                case "mul" -> a.multiply(b);
                case "div" -> a.divide(b, MathContext.DECIMAL64);
                default -> BigDecimal.ZERO;
            };
            return Map.of("result", r.toPlainString());
        } catch (Exception e) { return Map.of("error", e.getMessage()); }
    }
}

