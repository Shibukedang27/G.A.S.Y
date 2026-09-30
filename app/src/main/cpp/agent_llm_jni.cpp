#include <jni.h>
#include <string>
#include <vector>
#include <cstring>
#include "llama.h"

static llama_model * g_model = nullptr;
static llama_context * g_ctx = nullptr;

extern "C" JNIEXPORT jboolean JNICALL Java_com_agenthitler_NativeLlm_nativeLoadModel(JNIEnv * env, jobject, jstring path) {
    const char * p = env->GetStringUTFChars(path, nullptr);
    llama_backend_init();
    auto mp = llama_model_default_params();
    mp.n_gpu_layers = 0;
    g_model = llama_model_load_from_file(p, mp);
    env->ReleaseStringUTFChars(path, p);
    if (!g_model) return JNI_FALSE;
    auto cp = llama_context_default_params();
    cp.n_ctx = 1024; cp.n_batch = 512; cp.n_threads = 4; cp.n_threads_batch = 4;
    g_ctx = llama_init_from_model(g_model, cp);
    if (!g_ctx) { llama_model_free(g_model); g_model = nullptr; return JNI_FALSE; }
    return JNI_TRUE;
}
extern "C" JNIEXPORT void JNICALL Java_com_agenthitler_NativeLlm_nativeUnloadModel(JNIEnv*, jobject) {
    if (g_ctx) { llama_free(g_ctx); g_ctx = nullptr; }
    if (g_model) { llama_model_free(g_model); g_model = nullptr; }
}
extern "C" JNIEXPORT jboolean JNICALL Java_com_agenthitler_NativeLlm_nativeIsLoaded(JNIEnv*, jobject) { return g_model && g_ctx ? JNI_TRUE : JNI_FALSE; }
extern "C" JNIEXPORT jstring JNICALL Java_com_agenthitler_NativeLlm_nativeGenerate(JNIEnv * env, jobject, jstring prompt, jint maxTokens) {
    if (!g_model || !g_ctx) return env->NewStringUTF("");
    const char * text = env->GetStringUTFChars(prompt, nullptr);
    const llama_vocab * vocab = llama_model_get_vocab(g_model);
    int32_t n = llama_tokenize(vocab, text, (int32_t)strlen(text), nullptr, 0, true, false);
    if (n >= 0) { env->ReleaseStringUTFChars(prompt, text); return env->NewStringUTF(""); }
    std::vector<llama_token> toks((size_t)-n); n = llama_tokenize(vocab, text, (int32_t)strlen(text), toks.data(), (int32_t)toks.size(), true, false); env->ReleaseStringUTFChars(prompt, text);
    auto batch = llama_batch_init(512, 0, 1); std::string out;
    for (int32_t i=0;i<n;i++){batch.token[i]=toks[i];batch.pos[i]=i;batch.n_seq_id[i]=1;batch.seq_id[i][0]=0;batch.logits[i]=(i==n-1);}
    batch.n_tokens=n; if(llama_decode(g_ctx,batch)!=0){llama_batch_free(batch);return env->NewStringUTF("");}
    auto sp=llama_sampler_chain_default_params(); auto * sampler=llama_sampler_chain_init(sp); llama_sampler_chain_add(sampler,llama_sampler_init_greedy());
    int32_t pos=n; for(int i=0;i<maxTokens;i++){auto id=llama_sampler_sample(sampler,g_ctx,-1);if(llama_vocab_is_eog(vocab,id))break;char buf[256];int k=llama_token_to_piece(vocab,id,buf,sizeof(buf),0,false);if(k>0)out.append(buf,k);batch.token[0]=id;batch.pos[0]=pos++;batch.n_seq_id[0]=1;batch.seq_id[0][0]=0;batch.logits[0]=1;batch.n_tokens=1;if(llama_decode(g_ctx,batch)!=0)break;}
    llama_sampler_free(sampler); llama_batch_free(batch); return env->NewStringUTF(out.c_str());
}
