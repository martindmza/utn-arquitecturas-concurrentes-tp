# utn-arquitecturas-concurrentes-tp
TP de arquitecturas concurrentes grupo 4
## descripcion TP: https://docs.google.com/document/d/1GQYLL-Iaisku31sYLwNYZn6bvHTeAoPsP2nnyLDHE98/edit?usp=sharing.

# Api endpoints

## Crear lista de tareas: 
  POST /list 
  body { 'listName': string, tasks: [ { 'taskName': string, 'order': number, 'status': string } ] }
  -H 'Authentication: JWT'

## Editar lista de tareas
  PUT /list/:id
  body { 'listName': string, tasks: [ { 'taskName': string, 'order': number, 'status': string } ] }
  -H 'Authentication: JWT'

## Ver lista
  GET /list/:id
  -H 'Authentication: JWT'
  response:
  { 'listName': string, tasks: [ { 'taskName': string, 'order': number, 'status': string } ] }

# Extra documentación

## Vert.x https://vertx.io/blog/building-a-real-time-web-app-with-angular-ngrx-and-vert-x/
## node vs Vertx: https://www.cubrid.org/blog/3826505
