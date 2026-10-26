const SUPABASE_URL = 'https://qyuczfpgydgqywgthgrz.supabase.co';
const SUPABASE_PUBLISHABLE_KEY = 'sb_publishable_2NLEuDREplLS9DgUXeA8Tw_f8MJOhCq';

const emailModal = document.getElementById('emailModal');
const openEmailList = document.getElementById('openEmailList');
const closeEmailModal = document.getElementById('closeEmailModal');
const emailAuthView = document.getElementById('emailAuthView');
const emailLoggedIn = document.getElementById('emailLoggedIn');
const emailAuthForm = document.getElementById('emailAuthForm');
const emailAddress = document.getElementById('emailAddress');
const emailPassword = document.getElementById('emailPassword');
const emailSubmit = document.getElementById('emailSubmit');
const emailMessage = document.getElementById('emailMessage');
const emailModalTitle = document.getElementById('emailModalTitle');
const emailModalSub = document.getElementById('emailModalSub');
const emailToggleText = document.getElementById('emailToggleText');
const toggleEmailMode = document.getElementById('toggleEmailMode');
const emailAvatar = document.getElementById('emailAvatar');
const emailName = document.getElementById('emailName');
const emailUserAddress = document.getElementById('emailUserAddress');
const emailLogout = document.getElementById('emailLogout');

let supabaseClient = null;
let currentUser = null;
let isSignUp = true;

function setMessage(text = '', type = '') {
  emailMessage.textContent = text;
  emailMessage.className = `email-message${type ? ` ${type}` : ''}`;
}

function setMode(signUp) {
  isSignUp = signUp;
  emailModalTitle.textContent = signUp ? 'Join email list' : 'Sign in';
  emailModalSub.textContent = signUp ? 'Stay updated with Floating Muse.' : 'Enter your email-list credentials.';
  emailSubmit.textContent = signUp ? 'Join email list' : 'Sign in';
  emailToggleText.textContent = signUp ? 'Already joined?' : 'New here?';
  toggleEmailMode.textContent = signUp ? 'Sign in' : 'Join email list';
  setMessage();
}

function showAccount(user) {
  currentUser = user;
  emailAuthView.hidden = true;
  emailLoggedIn.hidden = false;
  const name = user?.email?.split('@')[0] || 'Muse friend';
  emailAvatar.textContent = name.charAt(0).toUpperCase();
  emailName.textContent = name;
  emailUserAddress.textContent = user?.email || '';
}

function showAuth() {
  emailAuthView.hidden = false;
  emailLoggedIn.hidden = true;
}

function openModal() {
  emailModal.classList.add('active');
  emailModal.setAttribute('aria-hidden', 'false');
  if (currentUser) showAccount(currentUser);
  else {
    setMode(true);
    showAuth();
    setTimeout(() => emailAddress.focus(), 100);
  }
}

function closeModal() {
  emailModal.classList.remove('active');
  emailModal.setAttribute('aria-hidden', 'true');
  setMessage();
}

async function submitAuth(event) {
  event.preventDefault();
  const email = emailAddress.value.trim();
  const password = emailPassword.value;
  if (!email || !password) return setMessage('Please fill in both fields.', 'error');
  if (!supabaseClient) return setMessage('The email list is temporarily unavailable. Please try again soon.', 'error');

  emailSubmit.disabled = true;
  emailSubmit.textContent = isSignUp ? 'Joining…' : 'Signing in…';
  setMessage();
  try {
    if (isSignUp) {
      const { data, error } = await supabaseClient.auth.signUp({
        email,
        password,
        options: { emailRedirectTo: window.location.href.split('#')[0] }
      });
      if (error) throw error;
      if (!data.session) {
        setMessage('Please check your email to confirm your subscription.', 'info');
      } else {
        setMessage('Account created. Welcome to the list!', 'success');
        showAccount(data.user);
      }
    } else {
      const { data, error } = await supabaseClient.auth.signInWithPassword({ email, password });
      if (error) throw error;
      setMessage('Welcome back!', 'success');
      showAccount(data.user);
    }
  } catch (error) {
    let message = error.message || 'Something went wrong.';
    if (message.includes('Invalid login credentials')) message = 'Invalid email or password.';
    if (message.includes('User already registered')) message = 'This email is already registered. Try signing in.';
    setMessage(message, 'error');
  } finally {
    emailSubmit.disabled = false;
    emailSubmit.textContent = isSignUp ? 'Join email list' : 'Sign in';
  }
}

openEmailList.addEventListener('click', openModal);
closeEmailModal.addEventListener('click', closeModal);
emailModal.addEventListener('click', (event) => { if (event.target === emailModal) closeModal(); });
document.addEventListener('keydown', (event) => { if (event.key === 'Escape' && emailModal.classList.contains('active')) closeModal(); });
toggleEmailMode.addEventListener('click', () => { setMode(!isSignUp); emailAuthForm.reset(); });
emailAuthForm.addEventListener('submit', submitAuth);
emailLogout.addEventListener('click', async () => {
  if (supabaseClient) await supabaseClient.auth.signOut();
  currentUser = null;
  setMode(true);
  showAuth();
  emailAuthForm.reset();
});

if (window.supabase?.createClient) {
  supabaseClient = window.supabase.createClient(SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY);
  supabaseClient.auth.onAuthStateChange((_event, session) => {
    currentUser = session?.user || null;
    if (currentUser && emailModal.classList.contains('active')) showAccount(currentUser);
  });
  supabaseClient.auth.getSession().then(({ data }) => { currentUser = data.session?.user || null; });
}
