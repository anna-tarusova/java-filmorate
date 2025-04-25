# java-filmorate
Template repository for Filmorate project.

![Диаграмма](https://github.com/anna-tarusova/java-filmorate/blob/add-friends-likes-with-db-scheme/Filmorate%20Diagram.png?raw=true)

![Диаграмма](https://github.com/anna-tarusova/java-filmorate/blob/main/Filmorate%20Diagram.png?raw=true)

``` sql
SELECT f.name,
       u.name
FROM Film f
JOIN Film_Likes fl ON f.id =fl.film_id
JOIN User u ON u.id = fl.user_id;
```
``` sql
SELECT f.name,
       g.name,
       mr.name
FROM Film f
JOIN Genre g ON f.genre_id = g.id
JOIN MPA_Rating mr ON f.mpa_rating_id = mr_id;
```
``` sql
SELECT u2.*
FROM User u1
JOIN Friendship f ON u1.id = f.user_id
JOIN User u2 ON u2.id = f.friend_id
WHERE u1.login = '';
```
