import {Component, NgZone} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpClient, HttpDownloadProgressEvent, HttpEventType} from '@angular/common/http';
import {MarkdownComponent} from 'ngx-markdown';
import {environment} from '../../environments/environment';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [FormsModule, MarkdownComponent],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class Chat {
  protected question: string = "";
  protected response: string = "";
  protected errorMsg: string = "";
  protected progress: boolean = false;
  protected isListening: boolean = false;
  protected isSpeaking: boolean = false;
  protected selectedFiles: File[] = [];
  protected autoSpeak: boolean = false;

  private recognition: any;
  private synthesis = window.speechSynthesis;
  private currentAudio: HTMLAudioElement | null = null;

  constructor(private http: HttpClient, private zone: NgZone) {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (SpeechRecognition) {
      this.recognition = new SpeechRecognition();
      this.recognition.lang = 'fr-FR';
      this.recognition.continuous = false;
      this.recognition.interimResults = false;
      this.recognition.onresult = (event: any) => {
        this.zone.run(() => {
          this.question = event.results[0][0].transcript;
          this.isListening = false;
          this.selectedFiles.length ? this.askWithPdf() : this.askAgent();
        });
      };
      this.recognition.onerror = (event: any) => {
        this.zone.run(() => {
          this.isListening = false;
          this.errorMsg = 'Erreur micro: ' + (event.error || 'inconnue');
        });
      };
      this.recognition.onend = () => {
        this.zone.run(() => { this.isListening = false; });
      };
    }
  }

  protected askAgent() {
    if (!this.question.trim()) return;
    this.response = "";
    this.errorMsg = "";
    this.progress = true;
    this.http
      .get(`${environment.apiUrl}/askAgent?question=` + encodeURIComponent(this.question),
        {responseType: 'text', observe: "events", reportProgress: true})
      .subscribe({
        next: (resp: any) => {
          if (resp.type === HttpEventType.DownloadProgress) {
            this.response = (resp as HttpDownloadProgressEvent).partialText ?? "";
          } else if (resp.type === HttpEventType.Response) {
            this.response = resp.body ?? "";
          }
        },
        error: (err: any) => { console.log(err); this.errorMsg = err?.message || 'Erreur lors de la requête'; this.progress = false; },
        complete: () => {
          this.progress = false;
          if (this.autoSpeak) this.speakResponse();
        }
      });
  }

  protected askWithPdf() {
    if (!this.selectedFiles.length) return;
    this.response = "";
    this.errorMsg = "";
    this.progress = true;
    const formData = new FormData();
    this.selectedFiles.forEach(f => formData.append('files', f));
    formData.append('question', this.question || 'Analyse ces documents et résume leur contenu');
    this.http
      .post(`${environment.apiUrl}/askWithPdf`, formData,
        {responseType: 'text', observe: 'events', reportProgress: true})
      .subscribe({
        next: (resp: any) => {
          if (resp.type === HttpEventType.DownloadProgress) {
            this.response = (resp as HttpDownloadProgressEvent).partialText ?? "";
          } else if (resp.type === HttpEventType.Response) {
            this.response = resp.body ?? "";
          }
        },
        error: (err: any) => { console.log(err); this.errorMsg = err?.error || err?.message || 'Erreur lors de l\'envoi du PDF'; this.progress = false; },
        complete: () => {
          this.progress = false;
          if (this.autoSpeak) this.speakResponse();
        }
      });
  }

  protected onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.selectedFiles = Array.from(input.files);
    }
  }

  protected removeFile(index: number) {
    this.selectedFiles.splice(index, 1);
  }

  protected toggleVoice() {
    if (!this.recognition) {
      alert('La reconnaissance vocale n\'est pas supportée par ce navigateur.');
      return;
    }
    if (this.isListening) {
      this.recognition.stop();
    } else {
      this.isListening = true;
      this.recognition.start();
    }
  }

  protected speakResponse() {
    if (!this.response) return;
    const plainText = this.response.replace(/[#*`_~\[\]()>]/g, '').replace(/\n+/g, ' ').trim();
    this.isSpeaking = true;
    this.http.post(`${environment.apiUrl}/tts`, plainText, {
      responseType: 'arraybuffer',
      headers: {'Content-Type': 'text/plain'}
    }).subscribe({
      next: (audioData) => {
        const blob = new Blob([audioData], {type: 'audio/wav'});
        const url = URL.createObjectURL(blob);
        const audio = new Audio(url);
        this.currentAudio = audio;
        audio.onended = () => { this.isSpeaking = false; URL.revokeObjectURL(url); this.currentAudio = null; };
        audio.onerror = () => { this.isSpeaking = false; URL.revokeObjectURL(url); this.currentAudio = null; };
        audio.play();
      },
      error: () => this.speakWithBrowser(plainText)
    });
  }

  private speakWithBrowser(text: string) {
    if (!this.synthesis) { this.isSpeaking = false; return; }
    this.synthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'fr-FR';
    utterance.rate = 1;
    const voices = this.synthesis.getVoices();
    const frVoices = voices.filter(v => v.lang.startsWith('fr'));
    const preferred = frVoices.find(v => v.name.includes('Google') || v.name.includes('Microsoft'));
    if (preferred) utterance.voice = preferred;
    else if (frVoices.length) utterance.voice = frVoices[0];
    utterance.onend = () => { this.isSpeaking = false; };
    this.synthesis.speak(utterance);
  }

  protected stopSpeaking() {
    if (this.currentAudio) { this.currentAudio.pause(); this.currentAudio = null; }
    this.synthesis.cancel();
    this.isSpeaking = false;
  }
}
