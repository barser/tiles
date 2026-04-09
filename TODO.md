# TODO

## Навигация
- [ ] Реализовать кнопку "Up" навигации в `ToDoActivity`, `HistoryActivity`, `GameDetailActivity` (добавить `NavigationIcon` с `onBackPressed()` в `Scaffold` topBar)

## Валидация
- [ ] Добавить валидацию даты с обратной связью пользователю в `PlayResultDialog` — при некорректном формате показывать ошибку через `supportingText` в `OutlinedTextField` вместо молчаливого fallback на `now`

## Тесты
- [ ] Покрыть тестами ViewModel (`ToDoViewModel`, `HistoryViewModel`, `GameDetailViewModel`)
- [ ] Покрыть тестами Repository (`GameRepository`, `PlayResultRepository`)
