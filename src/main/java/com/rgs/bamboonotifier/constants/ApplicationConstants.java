package com.rgs.bamboonotifier.constants;

public class ApplicationConstants {

    public static final String NEW_DEPLOY_MESSAGE_TEMPLATE = """
    <b>Новый деплой на стенде %s</b>
    
    📦 Версия: %s
    🕓 Время начала: %s
    🕔 Время завершения: %s
    👨‍💻 Автор: %s
    🌿 Бранч: %s
    📊 Конечный статус: %s
    📊 Статус деплоя: %s
    """;
}
