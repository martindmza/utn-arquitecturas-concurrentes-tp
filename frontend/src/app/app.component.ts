import { Component, OnInit } from '@angular/core';
import { ApiService } from './api/ApiService';
import { Task } from './Task';
import { first } from 'rxjs/operators';
import { TaskList } from './TaskList';
import { interval, Subscription } from 'rxjs';
import { TaskListResponse } from './api/TaskListResponse';
import { TaskResponse } from './api/TaskResponse';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  subscription: Subscription;
  tasksList: TaskList[] = [];
  selectedTask: Task = new Task();
  selectedTaskList: TaskList = new TaskList();
  newName: string = "";
  userHasSelected = false;
  editingTaskListName = false;

  constructor(
    private apiService: ApiService
  ) {}

  ngOnInit(): void {
    this.loadTaskLists();
    const source = interval(10000);
    this.subscription = source.subscribe(val => this.loadTaskLists());
  }

  selectTask(task: Task) {
    this.userHasSelected = true;
    this.selectedTask = task;
    this.newName = this.selectedTask.name;
  }

  selectTaskList(taskList: TaskList) {
    this.selectedTaskList = taskList;
    this.userHasSelected = false;
    this.selectedTask = new Task();
    this.newName = "";
  }

  keyPress(event: KeyboardEvent) {
    if(event.code === 'Enter'){
      if (this.editingTaskListName) {
        this.updateTaskListName();
      } else {
        this.updateTaskName();
      }
    }
  }

  updateTaskName() {
    this.selectedTask.name = this.newName;
    this.apiService.updateTask(this.selectedTaskList.id, this.selectedTask)
    .pipe(first())
    .subscribe(
      data => {
        this.selectedTask.name = data.data.name;
      error => {}
    });
  }

  moveUp() {
    if (this.selectedTask.position == 1) {
      return;
    }

    this.selectedTaskList.tasks.forEach( task => {
      if (task.position == this.selectedTask.position - 1) {
        task.position++;
        this.apiService.updateTask(this.selectedTaskList.id, task)
        .pipe(first())
        .subscribe(
          data => {
            //task.position = data.data.position;
          error => {}
        });
      }
    });
    this.selectedTask.position--;
    this.apiService.updateTask(this.selectedTaskList.id, this.selectedTask)
    .pipe(first())
    .subscribe(
      data => {
        //this.selectedTask.position = data.data.position;
      error => {}
    });

    this.reSortList();
  }

  moveDown() {
    if (this.selectedTask.position == this.selectedTaskList.tasks.length) {
      return;
    }

    this.selectedTaskList.tasks.forEach( task => {
      if (task.position == this.selectedTask.position + 1) {
        task.position--;
        this.apiService.updateTask(this.selectedTaskList.id, task)
        .pipe(first())
        .subscribe(
          data => {
            //task.position = data.data.position;
          error => {}
        });
      }
    });
    this.selectedTask.position++;
    this.apiService.updateTask(this.selectedTaskList.id, this.selectedTask)
    .pipe(first())
    .subscribe(
      data => {
        //this.selectedTask.position = data.data.position;
      error => {}
    });
    this.reSortList();
  }

  reSortList = () => this.selectedTaskList.tasks.sort((a,b) => a.position - b.position);

  reSortTaskList = (taskList: TaskList) => taskList.tasks.sort((a,b) => a.position - b.position);

  changeStatus = () => {
    this.selectedTask.done = !this.selectedTask.done;
    this.apiService.updateTask(this.selectedTaskList.id, this.selectedTask)
    .pipe(first())
    .subscribe(
      data => {
      error => {}
    });
    for (var task of this.selectedTaskList.tasks) {
      if (task.position === this.selectedTask.position + 1) {
        this.selectTask(task);
        break;    
      }
    }
  }

  removeTask() {
    this.newName = "";
    this.userHasSelected = false;
    this.apiService.deleteTask(this.selectedTask)
    .pipe(first())
    .subscribe(
      data => {
        this.selectedTaskList.tasks = this.selectedTaskList.tasks.filter( task => task.id != this.selectedTask.id );
        this.selectedTask = new Task();
      error => {}
    });
  }

  newTask() {
    const newTask = new Task();
    newTask.position = this.selectedTaskList.tasks.length + 1;
    if (this.userHasSelected) {
      this.selectedTaskList.tasks.forEach( task => {
        if (task.position > this.selectedTask.position) {
          task.position++;
          this.apiService.updateTask(this.selectedTaskList.id, task)
          .pipe(first())
          .subscribe(
            data => {
              task.position = data.data.position;
            error => {}
          });
        }
      });
      newTask.position = this.selectedTask.position + 1;
    }
    this.apiService.createTask(this.selectedTaskList.id, newTask)
    .pipe(first())
    .subscribe(
      data => {
        newTask.id = data.data.id;
        this.selectedTaskList.tasks.push(newTask);
      error => {}
    });
  }

  updatingTaskListName() {
    this.editingTaskListName = true;
    this.userHasSelected = false;
    this.selectedTask = new Task();
    this.newName = "";
  }

  updateTaskListName() {
    this.editingTaskListName = false;
    this.apiService.updateTaskList(this.selectedTaskList)
    .pipe(first())
    .subscribe(
      data => {
        
      error => {}
  });
  }


  // ============================= REST ===================================================================================

  newTaskList() {
    this.apiService.createTaskList()
      .pipe(first())
      .subscribe(
        data => {
          this.tasksList.push(this.buildTaskList(data.data));
        error => {}
    });
  }

  removeTaskList() {
    this.selectedTaskList.tasks = [];
    this.apiService.deleteTaskList(this.selectedTaskList)
    .pipe(first())
    .subscribe(
      data => {
        this.selectedTaskList = new TaskList();
        if (this.tasksList.length > 1) {
          this.selectedTaskList = this.tasksList[0];
        }
        this.loadTaskLists();
      error => {}
    });
  }

  loadTaskLists() {
    this.apiService.getTaskLists()
      .pipe(first())
      .subscribe(
        data => {

          this.tasksList.forEach( tl => tl.found = false );

          // intersection
          data.lists.forEach( dataTl => {
            this.tasksList.forEach( tl => {
              if (dataTl.id === tl.id) {
                dataTl.found = 1;
                tl.found = true;
                tl.name = dataTl.listName;

                const foundTaskIdsFromApi = [];
                tl.tasks.forEach( t => t.found = false );
                dataTl.task_ids.forEach( dataTid => {
                  tl.tasks.forEach( t => {
                    if (dataTid === t.id) {
                      foundTaskIdsFromApi.push(dataTid);
                      t.found = true;
                      this.updateTask(tl, t);
                    }
                  });
                });

                // new tasks
                const newTasks =  dataTl.task_ids.filter( tid => !foundTaskIdsFromApi.includes(tid));
                newTasks.forEach( tid => {
                  this.loadTask(tl, tid);
                });

                //deleted tasks
                tl.tasks = tl.tasks.filter( t => t.found );
              }
            });
          });

          // new lists
          const newTaskLists = data.lists.filter( tl => !tl.found );
          newTaskLists.forEach( tl => {
            const newTaskList = this.buildTaskList(tl);
            this.tasksList.push(newTaskList);
            tl.task_ids.forEach( t => this.loadTask(newTaskList, t));
          });

          //deleted lists
          const deletedTasksLists = this.tasksList.filter( t => !t.found );
          if (deletedTasksLists.map( tl => tl.id ).includes(this.selectedTaskList.id)) {
            this.editingTaskListName = false;
            this.userHasSelected = false;
            this.selectedTask = new Task();
            this.selectedTaskList = new TaskList();
            this.newName = "";
          }
          deletedTasksLists.forEach( tl => tl.tasks = [] );
          this.tasksList = this.tasksList.filter( tl => tl.found );

        error => {}
    });
  }

  loadTask(taskList: TaskList, taskId: string) {
    this.apiService.getTask(taskId)
    .pipe(first())
    .subscribe(
      data => {
        taskList.tasks.push(this.buildTask(data));
        this.reSortTaskList(taskList);
        error => {}
    });
  }

  updateTask(taskList: TaskList, task: Task) {
    this.apiService.getTask(task.id)
    .pipe(first())
    .subscribe(
      data => {
        task.position = data.position;
        task.name = data.name;
        task.done = data.done;
        this.reSortTaskList(taskList);
        error => {

        }
    });
  }

  buildTaskList(taskListResponse: TaskListResponse): TaskList {
    const newTaskList = new TaskList();
    newTaskList.id = taskListResponse.id;
    newTaskList.name = taskListResponse.listName;
    newTaskList.task_ids = taskListResponse.task_ids;
    newTaskList.found = true;
    return newTaskList;
  }

  buildTask(taskResponse: TaskResponse):Task {
    const newTask = new Task();
    newTask.id = taskResponse.id;
    newTask.name = taskResponse.name;
    newTask.done = taskResponse.done;
    newTask.position = taskResponse.position;
    return newTask;
  }
}