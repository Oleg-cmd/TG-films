#!/bin/zsh
# Проверяем, существует ли файл .env
if [[ ! -f ".env" ]]; then
  echo "Ошибка: Файл .env не найден."
  exit 0
fi

# Читаем файл .env построчно
while IFS= read -r line; do
  # Игнорируем пустые строки и комментарии
  if [[ -n "$line" ]] && [[ ! "$line" =~ ^# ]]; then
    # Разделяем строку на ключ и значение по знаку "="
    key="${line%%=*}" # zsh syntax for splitting string
    value="${line#*=}" # zsh syntax for splitting string
    value="${value//\"/}" # remove quotes in zsh

    # Экспортируем переменную
    export "$key=$value"

  fi
done < .env

echo "Переменные из .env экспортированы."