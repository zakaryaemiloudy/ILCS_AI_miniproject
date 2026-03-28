import {Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpClient, HttpDownloadProgressEvent, HttpEventType, HttpProgressEvent} from '@angular/common/http';
import {MarkdownComponent} from 'ngx-markdown';

@Component({
  selector: 'app-chat',
  imports: [
    FormsModule,
    MarkdownComponent
  ],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class Chat {
  question: string="";
  reponse : any;
  progress:boolean=false;
  constructor(private http: HttpClient) {
  }
  askAgent() {
    this.reponse="";
    this.progress=true;
    this.http
      .get("http://localhost:8080/askAgent?question="+this.question
      ,{responseType:'text',
        observe:"events",reportProgress:true})
      .subscribe({
        next:resp=>{
          if(resp.type=== HttpEventType.DownloadProgress){
            this.reponse=(resp as HttpDownloadProgressEvent).partialText;
          }

        },
        error:err => {
          console.log(err);
        },
        complete:()=>{
          this.progress=false;
        }
      })
  }
}
