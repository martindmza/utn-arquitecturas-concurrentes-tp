export class Task {
    id: string;
    name: string = "";
    position: number = -1;
    done: boolean = false;
    found: boolean;

    constructor() {
        this.found = false;
    }
}