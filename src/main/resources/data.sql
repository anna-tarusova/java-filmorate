MERGE INTO public.GENRE
(Id, NAME)
VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Боевик'),
(4, 'Трагедия'),
(5, 'Триллер'),
(6, 'Трагикомедия');

MERGE INTO public.MPA_RATING
(Id, Name)
VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');