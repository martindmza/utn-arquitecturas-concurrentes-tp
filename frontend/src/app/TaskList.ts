import { Task } from "./Task";

export class TaskList {
    id: string;
    name: string = "";
    tasks: Task[] = [];
    task_ids: string[] = [];
    found: boolean;

    constructor() {
        this.found = false;
    }
}