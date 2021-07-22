import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Task } from '../Task';
import { TaskList } from '../TaskList';
import { TaskListsResponse } from './TaskListsResponse';
import { TaskResponse } from './TaskResponse';
import { TaskListRequest } from './TaskListRequest';
import { TaskListResponse } from './TaskListResponse';
import { TaslListCreationResponse } from './TaskListCreationResponse';
import { TaskListCreationRequest } from './TaskCreationRequest';
import { TaskPostResponse } from './TaskPostResponse';

@Injectable({ providedIn: 'root' })
export class ApiService {

    //apiUrl: String = "http://localhost:8183";
    apiUrl: String = "http://190.245.170.108:7779";
    auth = {
        headers: {'Authorization':'Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoidXN1YXJpbzEiLCJpYXQiOjE2MjY3NTQ5NTV9.1_PdbtPCImf78rvcCE7X_Vufky1RorhdPtLFdgFPHdA'}
    };

    constructor(private http: HttpClient) { }

    //Tasks Lists
    getTaskLists() {
        return this.http.get<TaskListsResponse>(`${this.apiUrl}/app/lists`, this.auth);
    }

    createTaskList() {
        const list = new TaskListRequest();
        list.task_ids = [];
        list.listName = "";
        return this.http.post<TaslListCreationResponse>(`${this.apiUrl}/app/list`, list, this.auth);
    }

    updateTaskList(taskList: TaskList) {
        const list = new TaskListRequest();
        list.listName = taskList.name;
        list.task_ids = taskList.tasks.map( t => t.id );
        list.id = taskList.id;
        return this.http.post<TaslListCreationResponse>(`${this.apiUrl}/app/list`, list, this.auth);

    }

    deleteTaskList(taskList: TaskList) {
        return this.http.delete<TaskList>(`${this.apiUrl}/app/list/${taskList.id}`, this.auth);
    }



    // Tasks
    getTask(taskId: String) {
        return this.http.get<TaskResponse>(`${this.apiUrl}/app/task/${taskId}`, this.auth);
    }

    createTask(taskListId: string, task: Task) {
        const newTaskRequest = new TaskListCreationRequest();
        newTaskRequest.done = false;
        newTaskRequest.position = task.position
        newTaskRequest.list = taskListId;
        return this.http.post<TaskPostResponse>(`${this.apiUrl}/app/task`, newTaskRequest, this.auth);
    }

    updateTask(taskListId: string, task: Task) {
        const newTaskRequest = new TaskListCreationRequest();
        newTaskRequest.id = task.id;
        newTaskRequest.name = task.name;
        newTaskRequest.done = task.done;
        newTaskRequest.position = task.position
        newTaskRequest.list = taskListId;
        return this.http.post<TaskPostResponse>(`${this.apiUrl}/app/task`, newTaskRequest , this.auth);
    }

    deleteTask(task: Task) {
        return this.http.delete<Task>(`${this.apiUrl}/app/task/${task.id}`, this.auth);
    }
}