# TODO

## Навигация
- [x] Реализовать кнопку "Up" навигации в `ToDoActivity`, `HistoryActivity`, `GameDetailActivity` (добавить `NavigationIcon` с `onBackPressed()` в `Scaffold` topBar)

## Валидация
- [x] Добавить валидацию даты с обратной связью пользователю в `PlayResultDialog` — при некорректном формате показывать ошибку через `supportingText` в `OutlinedTextField` вместо молчаливого fallback на `now`

## Тесты
- [x] Покрыть тестами ViewModel (`ToDoViewModel`, `HistoryViewModel`, `GameDetailViewModel`)
- [x] Покрыть тестами Repository (`GameRepository`, `PlayResultRepository`)
- [x] Покрыть тестами DAO (`GameDao`, `PlayResultDao`)

## Разное от автора
-[ ] При обновлении версий не сохраняются данные (как будто бы). Надо проверить этот момент и гарантировать сохранение данных в случае миграций (обновлений).
