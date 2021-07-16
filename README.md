# utn-arquitecturas-concurrentes-tp
TP de arquitecturas concurrentes grupo 4
## descripcion TP: https://docs.google.com/document/d/1GQYLL-Iaisku31sYLwNYZn6bvHTeAoPsP2nnyLDHE98/edit?usp=sharing.

# Api endpoints

## endpoints de listas de tareas
```
GET /api/v1/taskLists -H "Content-Type: application/json"

[{
		"id": "1",
		"name": "Lista 1",
		"tasks": [{
				"id": "1",
				"name": "Tarea 1",
				"position": 1,
				"done": false
			},
			{
				"id": "2",
				"name": "tarea 2",
				"position": 2,
				"done": false
			}
		]
	},
	{
		"id": "2",
		"name": "Lista 2",
		"tasks": [{
				"id": "1",
				"name": "Tarea 1",
				"position": 1,
				"done": false
			},
			{
				"id": "3",
				"name": "Tarea 2",
				"position": 2,
				"done": false
			}
		]
	}
]
HTTP/1.1 200
```

```
GET /api/v1/taskLists/1 -H "Content-Type: application/json"

{
  "id": "1",
  "name": "Lista 1",
  "tasks": [{
      "id": "1",
      "name": "Tarea 1",
      "position": 1,
      "done": false
    },
    {
      "id": "2",
      "name": "Tarea 2",
      "position": 2,
      "done": false
    }
  ]
}
HTTP/1.1 200
```

```
POST /api/v1/taskLists -H "Content-Type: application/json"
-d '{"name": "Nueva task"}'

{
  "id": "1",
  "name": "Nueva task",
  "tasks": []
}
HTTP/1.1 200
```

```
PATCH /api/v1/taskLists/1 -H "Content-Type: application/json"
-d '{"name": "Nuevo nombre"}'

{
  "id": "1",
  "name": "Nuevo nombre",
  "tasks": [ ... ]
}

HTTP/1.1 200
```

```
DELETE /api/v1/taskLists/2

HTTP/1.1 200
```

## endpoints de tareas
```
GET api/v1/taskLists/1/tasks/1 -H "Content-Type: application/json"

{
  "id": "1",
  "name": "Tarea 1",
  "position": 1,
  "done": false
}
HTTP/1.1 200
```

```
POST /api/v1/taskLists/1/tasks -H "Content-Type: application/json" 
-d '{"name": "nueva tarea"}'

{
  "id": "2",
  "name": "Tarea 2",
  "position": 2,
  "done": false
}
HTTP/1.1 200
```

```
PATCH /api/v1/taskLists/1/tasks/2  -H "Content-Type: application/json"
-d '{"name": "name changed", "position": "99", "done": "true"}'

{
  "id": "2",
  "name": "name changed",
  "position": 99,
  "done": true
}
HTTP/1.1 200
```

```
DELETE /api/v1/taskLists/1/tasks/2

HTTP/1.1 200
```

# Extra documentación

## Vert.x https://vertx.io/blog/building-a-real-time-web-app-with-angular-ngrx-and-vert-x/
## node vs Vertx: https://www.cubrid.org/blog/3826505
