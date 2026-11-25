/* tools/inline_handlers_table.py 가 만든 파일. 손으로 고치지 않는다. */
/* 고정된 data-on* 글 → 처리기 함수 (this = 요소, event = 이벤트) */
window.__inlineHandlerTable = {
  "\n                        let v = this.value.replace(/[^0-9]/g, '');\n                        if (parseInt(v) > 10000000) v = '10000000';\n                        this.value = v ? Number(v).toLocaleString() : '';": function (event) {

                        let v = this.value.replace(/[^0-9]/g, '');
                        if (parseInt(v) > 10000000) v = '10000000';
                        this.value = v ? Number(v).toLocaleString() : '';
  },
  "_cancelGenLoading()": function (event) {
_cancelGenLoading()
  },
  "_loadExpenses(_budgetSelectedTripId)": function (event) {
_loadExpenses(_budgetSelectedTripId)
  },
  "_placeSnapshotClick(this)": function (event) {
_placeSnapshotClick(this)
  },
  "addDay()": function (event) {
addDay()
  },
  "addLedgerExpense()": function (event) {
addLedgerExpense()
  },
  "addPlanItem(this)": function (event) {
addPlanItem(this)
  },
  "changeSlide(-1)": function (event) {
changeSlide(-1)
  },
  "changeSlide(1)": function (event) {
changeSlide(1)
  },
  "checkAndOpenWrite()": function (event) {
checkAndOpenWrite()
  },
  "checkDuplicateId()": function (event) {
checkDuplicateId()
  },
  "checkPw(this)": function (event) {
checkPw(this)
  },
  "checkPw2(this)": function (event) {
checkPw2(this)
  },
  "closeCommunityPlanPreview()": function (event) {
closeCommunityPlanPreview()
  },
  "closeEditCommentModal()": function (event) {
closeEditCommentModal()
  },
  "closeNotifPopup()": function (event) {
closeNotifPopup()
  },
  "closePrev()": function (event) {
closePrev()
  },
  "closePrev();go('planner')": function (event) {
closePrev();go('planner')
  },
  "closePwResetModal()": function (event) {
closePwResetModal()
  },
  "closeQueue()": function (event) {
closeQueue()
  },
  "closeReportAction()": function (event) {
closeReportAction()
  },
  "closeSuspendModal()": function (event) {
closeSuspendModal()
  },
  "closeUnsavedChangesModal()": function (event) {
closeUnsavedChangesModal()
  },
  "closeWeatherPopup()": function (event) {
closeWeatherPopup()
  },
  "closeWithdrawModal()": function (event) {
closeWithdrawModal()
  },
  "closeWrite()": function (event) {
closeWrite()
  },
  "confirmLeaveAndSave()": function (event) {
confirmLeaveAndSave()
  },
  "confirmReportAction()": function (event) {
confirmReportAction()
  },
  "confirmSuspend()": function (event) {
confirmSuspend()
  },
  "confirmWithdraw()": function (event) {
confirmWithdraw()
  },
  "copyShareLink()": function (event) {
copyShareLink()
  },
  "createPlan()": function (event) {
createPlan()
  },
  "deleteCurrentPostFromDetail()": function (event) {
deleteCurrentPostFromDetail()
  },
  "doCommentWrite()": function (event) {
doCommentWrite()
  },
  "doConfirmPlan()": function (event) {
doConfirmPlan()
  },
  "doEditReviewSave()": function (event) {
doEditReviewSave()
  },
  "doLogout()": function (event) {
doLogout()
  },
  "doPlaceScrap()": function (event) {
doPlaceScrap()
  },
  "doPreviewScrap()": function (event) {
doPreviewScrap()
  },
  "doReviewLike()": function (event) {
doReviewLike()
  },
  "doReviewReport()": function (event) {
doReviewReport()
  },
  "doReviewScrap()": function (event) {
doReviewScrap()
  },
  "doSearch()": function (event) {
doSearch()
  },
  "doSignup()": function (event) {
doSignup()
  },
  "doSocialSignup()": function (event) {
doSocialSignup()
  },
  "doWithdraw()": function (event) {
doWithdraw()
  },
  "document.getElementById('confirmDoneModal').classList.remove('open')": function (event) {
document.getElementById('confirmDoneModal').classList.remove('open')
  },
  "document.getElementById('confirmDoneModal').classList.remove('open'); go('mypage'); setTimeout(function(){ if(typeof showMySection==='function') showMySection('trips', document.querySelector('.my-menu')); }, 150);": function (event) {
document.getElementById('confirmDoneModal').classList.remove('open'); go('mypage'); setTimeout(function(){ if(typeof showMySection==='function') showMySection('trips', document.querySelector('.my-menu')); }, 150);
  },
  "document.getElementById('editBlogEditor').focus();document.execCommand('bold')": function (event) {
document.getElementById('editBlogEditor').focus();document.execCommand('bold')
  },
  "document.getElementById('editBlogEditor').focus();document.execCommand('italic')": function (event) {
document.getElementById('editBlogEditor').focus();document.execCommand('italic')
  },
  "document.getElementById('editBlogEditor').focus();document.execCommand('underline')": function (event) {
document.getElementById('editBlogEditor').focus();document.execCommand('underline')
  },
  "document.getElementById('editImgInput').click()": function (event) {
document.getElementById('editImgInput').click()
  },
  "document.getElementById('mapPlaceModal').classList.remove('open')": function (event) {
document.getElementById('mapPlaceModal').classList.remove('open')
  },
  "document.getElementById('mapWeatherPopup').classList.remove('open')": function (event) {
document.getElementById('mapWeatherPopup').classList.remove('open')
  },
  "document.getElementById('notifPopup').style.display='none'": function (event) {
document.getElementById('notifPopup').style.display='none'
  },
  "document.getElementById('reportCommentModal').style.display='none'": function (event) {
document.getElementById('reportCommentModal').style.display='none'
  },
  "document.getElementById('reportPostModal').style.display='none'": function (event) {
document.getElementById('reportPostModal').style.display='none'
  },
  "document.getElementById('reviewDetailModal').classList.remove('open')": function (event) {
document.getElementById('reviewDetailModal').classList.remove('open')
  },
  "document.getElementById('shareModal').classList.remove('open')": function (event) {
document.getElementById('shareModal').classList.remove('open')
  },
  "document.getElementById('signupSuccessModal').style.display='none'; go('login');": function (event) {
document.getElementById('signupSuccessModal').style.display='none'; go('login');
  },
  "document.querySelector('nav').classList.toggle('nav-open')": function (event) {
document.querySelector('nav').classList.toggle('nav-open')
  },
  "event.preventDefault(); window._commUtil && window._commUtil.saveEditorSelection(document.getElementById('blogEditor'), '_communityWriteEditorRange')": function (event) {
event.preventDefault(); window._commUtil && window._commUtil.saveEditorSelection(document.getElementById('blogEditor'), '_communityWriteEditorRange')
  },
  "event.preventDefault(); window._commUtil && window._commUtil.saveEditorSelection(document.getElementById('communityEditContent'), '_communityEditEditorRange')": function (event) {
event.preventDefault(); window._commUtil && window._commUtil.saveEditorSelection(document.getElementById('communityEditContent'), '_communityEditEditorRange')
  },
  "event.stopPropagation();": function (event) {
event.stopPropagation();
  },
  "event.stopPropagation();clearSumField('acc')": function (event) {
event.stopPropagation();clearSumField('acc')
  },
  "event.stopPropagation();clearSumField('accopts')": function (event) {
event.stopPropagation();clearSumField('accopts')
  },
  "event.stopPropagation();clearSumField('budget')": function (event) {
event.stopPropagation();clearSumField('budget')
  },
  "event.stopPropagation();clearSumField('comp')": function (event) {
event.stopPropagation();clearSumField('comp')
  },
  "event.stopPropagation();clearSumField('date')": function (event) {
event.stopPropagation();clearSumField('date')
  },
  "event.stopPropagation();clearSumField('density')": function (event) {
event.stopPropagation();clearSumField('density')
  },
  "event.stopPropagation();clearSumField('dep')": function (event) {
event.stopPropagation();clearSumField('dep')
  },
  "event.stopPropagation();clearSumField('dest')": function (event) {
event.stopPropagation();clearSumField('dest')
  },
  "event.stopPropagation();clearSumField('food')": function (event) {
event.stopPropagation();clearSumField('food')
  },
  "event.stopPropagation();clearSumField('people')": function (event) {
event.stopPropagation();clearSumField('people')
  },
  "event.stopPropagation();clearSumField('pet')": function (event) {
event.stopPropagation();clearSumField('pet')
  },
  "event.stopPropagation();clearSumField('special')": function (event) {
event.stopPropagation();clearSumField('special')
  },
  "event.stopPropagation();clearSumField('style')": function (event) {
event.stopPropagation();clearSumField('style')
  },
  "event.stopPropagation();clearSumField('trans')": function (event) {
event.stopPropagation();clearSumField('trans')
  },
  "execAllReplace()": function (event) {
execAllReplace()
  },
  "execInvitedBulkDelete()": function (event) {
execInvitedBulkDelete()
  },
  "execMyRouteScrapBulkDelete()": function (event) {
execMyRouteScrapBulkDelete()
  },
  "execMyTripsBulkDelete()": function (event) {
execMyTripsBulkDelete()
  },
  "exportBudgetCSV()": function (event) {
exportBudgetCSV()
  },
  "exportBudgetPDF()": function (event) {
exportBudgetPDF()
  },
  "filterAdminUserRows()": function (event) {
filterAdminUserRows()
  },
  "generateScheduleAndGoMap(this)": function (event) {
generateScheduleAndGoMap(this)
  },
  "go('admin');return false": function (event) {
go('admin');return false
  },
  "go('community')": function (event) {
go('community')
  },
  "go('login')": function (event) {
go('login')
  },
  "go('main')": function (event) {
go('main')
  },
  "go('review')": function (event) {
go('review')
  },
  "go('signup')": function (event) {
go('signup')
  },
  "go('weather')": function (event) {
go('weather')
  },
  "go('weather');return false": function (event) {
go('weather');return false
  },
  "go(_loggedIn?'mypage':'login')": function (event) {
go(_loggedIn?'mypage':'login')
  },
  "goChangePw()": function (event) {
goChangePw()
  },
  "goHomeRefresh()": function (event) {
goHomeRefresh()
  },
  "goHomeRefresh();return false": function (event) {
goHomeRefresh();return false
  },
  "goLedger2()": function (event) {
goLedger2()
  },
  "goNewPlanner()": function (event) {
goNewPlanner()
  },
  "goPlanStep(1)": function (event) {
goPlanStep(1)
  },
  "goPlanStep(2)": function (event) {
goPlanStep(2)
  },
  "goPlanStep(3)": function (event) {
goPlanStep(3)
  },
  "goPlanStep(4)": function (event) {
goPlanStep(4)
  },
  "goRefresh('community')": function (event) {
goRefresh('community')
  },
  "goRefresh('community');return false": function (event) {
goRefresh('community');return false
  },
  "goRefresh('mypage')": function (event) {
goRefresh('mypage')
  },
  "goRefresh('weather')": function (event) {
goRefresh('weather')
  },
  "goToLedgerFromMap()": function (event) {
goToLedgerFromMap()
  },
  "gotoSite('kakao')": function (event) {
gotoSite('kakao')
  },
  "gotoSite('yeogi')": function (event) {
gotoSite('yeogi')
  },
  "if(event.key==='Enter'&&!event.shiftKey){event.preventDefault();sendMsg()}": function (event) {
if(event.key==='Enter'&&!event.shiftKey){event.preventDefault();sendMsg()}
  },
  "if(event.key==='Enter') doWithdraw()": function (event) {
if(event.key==='Enter') doWithdraw()
  },
  "if(event.key==='Enter')doCommentWrite()": function (event) {
if(event.key==='Enter')doCommentWrite()
  },
  "if(event.key==='Enter')prChangePassword()": function (event) {
if(event.key==='Enter')prChangePassword()
  },
  "if(event.key==='Enter')prSendCode()": function (event) {
if(event.key==='Enter')prSendCode()
  },
  "if(event.key==='Enter')prVerifyCode()": function (event) {
if(event.key==='Enter')prVerifyCode()
  },
  "if(event.key==='Enter')tryLogin()": function (event) {
if(event.key==='Enter')tryLogin()
  },
  "if(event.key==='Enter')verifyInfoPw()": function (event) {
if(event.key==='Enter')verifyInfoPw()
  },
  "if(event.target===this)closePrev()": function (event) {
if(event.target===this)closePrev()
  },
  "if(event.target===this)closePriceModal()": function (event) {
if(event.target===this)closePriceModal()
  },
  "if(event.target===this)closePwChangeRecommendModal()": function (event) {
if(event.target===this)closePwChangeRecommendModal()
  },
  "if(event.target===this)closeReportAction()": function (event) {
if(event.target===this)closeReportAction()
  },
  "if(event.target===this)closeSiteModal()": function (event) {
if(event.target===this)closeSiteModal()
  },
  "if(event.target===this)closeSuspendModal()": function (event) {
if(event.target===this)closeSuspendModal()
  },
  "if(event.target===this)closeWeatherPopup()": function (event) {
if(event.target===this)closeWeatherPopup()
  },
  "if(event.target===this)closeWithdrawModal()": function (event) {
if(event.target===this)closeWithdrawModal()
  },
  "if(event.target===this)closeWrite()": function (event) {
if(event.target===this)closeWrite()
  },
  "if(event.target===this)this.classList.remove('open')": function (event) {
if(event.target===this)this.classList.remove('open')
  },
  "insertEditImage(this)": function (event) {
insertEditImage(this)
  },
  "inviteShareMember(this)": function (event) {
inviteShareMember(this)
  },
  "loadAdminReports(this.value)": function (event) {
loadAdminReports(this.value)
  },
  "loadAdminStatistics()": function (event) {
loadAdminStatistics()
  },
  "loadAdminUsers(0,this.value)": function (event) {
loadAdminUsers(0,this.value)
  },
  "loadPlaceReviewsSorted(this.value)": function (event) {
loadPlaceReviewsSorted(this.value)
  },
  "loadPreviousPreference()": function (event) {
loadPreviousPreference()
  },
  "navPlannerClick()": function (event) {
navPlannerClick()
  },
  "navPlannerClick();return false": function (event) {
navPlannerClick();return false
  },
  "navPlannerClick();setNav(this)": function (event) {
navPlannerClick();setNav(this)
  },
  "onEndDateChange()": function (event) {
onEndDateChange()
  },
  "onOtherStayInput()": function (event) {
onOtherStayInput()
  },
  "onStartDateChange()": function (event) {
onStartDateChange()
  },
  "onTopPriceInput()": function (event) {
onTopPriceInput()
  },
  "openCommunityPlanPreview()": function (event) {
openCommunityPlanPreview()
  },
  "openCurrentPostEditModal()": function (event) {
openCurrentPostEditModal()
  },
  "openMapWeatherPopup()": function (event) {
openMapWeatherPopup()
  },
  "openNotificationPopup()": function (event) {
openNotificationPopup()
  },
  "openPwResetModal()": function (event) {
openPwResetModal()
  },
  "openQueue()": function (event) {
openQueue()
  },
  "openShareModal()": function (event) {
openShareModal()
  },
  "openWeatherPopup()": function (event) {
openWeatherPopup()
  },
  "pick(this)": function (event) {
pick(this)
  },
  "pick(this,'acc')": function (event) {
pick(this,'acc')
  },
  "pick(this,'acc');showOtherInput('other-acc',this)": function (event) {
pick(this,'acc');showOtherInput('other-acc',this)
  },
  "pick(this,'comp')": function (event) {
pick(this,'comp')
  },
  "pick(this,'comp');showOtherInput('other-comp',this)": function (event) {
pick(this,'comp');showOtherInput('other-comp',this)
  },
  "pick(this,'density')": function (event) {
pick(this,'density')
  },
  "pick(this,'edit-gender')": function (event) {
pick(this,'edit-gender')
  },
  "pick(this,'gender')": function (event) {
pick(this,'gender')
  },
  "pick(this,'trans')": function (event) {
pick(this,'trans')
  },
  "pick(this,'trans');showOtherInput('other-trans',this)": function (event) {
pick(this,'trans');showOtherInput('other-trans',this)
  },
  "pickVis(this)": function (event) {
pickVis(this)
  },
  "prChangePassword()": function (event) {
prChangePassword()
  },
  "prSendCode()": function (event) {
prSendCode()
  },
  "prShowStep(1)": function (event) {
prShowStep(1)
  },
  "prVerifyCode()": function (event) {
prVerifyCode()
  },
  "removeDay(this)": function (event) {
removeDay(this)
  },
  "resetAndBackToStep1()": function (event) {
resetAndBackToStep1()
  },
  "resetCurationForm()": function (event) {
resetCurationForm()
  },
  "resetInfoStep()": function (event) {
resetInfoStep()
  },
  "returnToLedgerSelector()": function (event) {
returnToLedgerSelector()
  },
  "saveCuration()": function (event) {
saveCuration()
  },
  "saveFormAndGoChat()": function (event) {
saveFormAndGoChat()
  },
  "saveInfoEdit()": function (event) {
saveInfoEdit()
  },
  "saveStayPrice()": function (event) {
saveStayPrice()
  },
  "sendAuthCode(this)": function (event) {
sendAuthCode(this)
  },
  "sendMsg()": function (event) {
sendMsg()
  },
  "setCommTab(this,'cafe')": function (event) {
setCommTab(this,'cafe')
  },
  "setCommTab(this,'food')": function (event) {
setCommTab(this,'food')
  },
  "setCommTab(this,'route')": function (event) {
setCommTab(this,'route')
  },
  "setCommTab(this,'stay')": function (event) {
setCommTab(this,'stay')
  },
  "setCommTab(this,'tour')": function (event) {
setCommTab(this,'tour')
  },
  "setShareTab(this,'members')": function (event) {
setShareTab(this,'members')
  },
  "setShareTab(this,'post')": function (event) {
setShareTab(this,'post')
  },
  "shareInviteToKakaoTalk()": function (event) {
shareInviteToKakaoTalk()
  },
  "showAdmin('curation',this)": function (event) {
showAdmin('curation',this)
  },
  "showAdmin('dashboard',this)": function (event) {
showAdmin('dashboard',this)
  },
  "showAdmin('reports',this)": function (event) {
showAdmin('reports',this)
  },
  "showAdmin('users',this)": function (event) {
showAdmin('users',this)
  },
  "showMySection('info',this)": function (event) {
showMySection('info',this)
  },
  "showMySection('invited',this)": function (event) {
showMySection('invited',this)
  },
  "showMySection('ledger',this)": function (event) {
showMySection('ledger',this)
  },
  "showMySection('likes',this)": function (event) {
showMySection('likes',this)
  },
  "showMySection('reviews',this)": function (event) {
showMySection('reviews',this)
  },
  "showMySection('scrap-cafe',this)": function (event) {
showMySection('scrap-cafe',this)
  },
  "showMySection('scrap-food',this)": function (event) {
showMySection('scrap-food',this)
  },
  "showMySection('scrap-route',this)": function (event) {
showMySection('scrap-route',this)
  },
  "showMySection('scrap-stay',this)": function (event) {
showMySection('scrap-stay',this)
  },
  "showMySection('scrap-tour',this)": function (event) {
showMySection('scrap-tour',this)
  },
  "showMySection('trips',this)": function (event) {
showMySection('trips',this)
  },
  "showMySection('withdraw',this)": function (event) {
showMySection('withdraw',this)
  },
  "snoozeAndClosePwChangeModal()": function (event) {
snoozeAndClosePwChangeModal()
  },
  "sortPosts(this.value)": function (event) {
sortPosts(this.value)
  },
  "startPlanFromCommunityPost()": function (event) {
startPlanFromCommunityPost()
  },
  "submitComment()": function (event) {
submitComment()
  },
  "submitEditComment()": function (event) {
submitEditComment()
  },
  "submitReportComment()": function (event) {
submitReportComment()
  },
  "submitReportPost()": function (event) {
submitReportPost()
  },
  "submitReview()": function (event) {
submitReview()
  },
  "switchLedgerTab('charts', this)": function (event) {
switchLedgerTab('charts', this)
  },
  "switchLedgerTab('history', this)": function (event) {
switchLedgerTab('history', this)
  },
  "switchMapTab('budget',this)": function (event) {
switchMapTab('budget',this)
  },
  "switchMapTab('map',this)": function (event) {
switchMapTab('map',this)
  },
  "switchMobileMapTab('budget')": function (event) {
switchMobileMapTab('budget')
  },
  "switchMobileMapTab('itinerary')": function (event) {
switchMobileMapTab('itinerary')
  },
  "switchMobileMapTab('map')": function (event) {
switchMobileMapTab('map')
  },
  "switchSumTab('tab-basic')": function (event) {
switchSumTab('tab-basic')
  },
  "switchSumTab('tab-extra')": function (event) {
switchSumTab('tab-extra')
  },
  "switchSumTab('tab-pref')": function (event) {
switchSumTab('tab-pref')
  },
  "this.closest('.overlay').classList.remove('open')": function (event) {
this.closest('.overlay').classList.remove('open')
  },
  "this.closest('.pdb-item').remove()": function (event) {
this.closest('.pdb-item').remove()
  },
  "this.style.borderColor='var(--border2)'": function (event) {
this.style.borderColor='var(--border2)'
  },
  "this.style.borderColor='var(--border2)'; this.style.boxShadow='none';": function (event) {
this.style.borderColor='var(--border2)'; this.style.boxShadow='none';
  },
  "this.style.borderColor='var(--sage)'": function (event) {
this.style.borderColor='var(--sage)'
  },
  "this.style.borderColor='var(--sage-l)'; this.style.boxShadow='var(--sh)';": function (event) {
this.style.borderColor='var(--sage-l)'; this.style.boxShadow='var(--sh)';
  },
  "this.style.display='none'": function (event) {
this.style.display='none'
  },
  "this.value=this.value.replace(/[^0-9]/g,''); if(parseInt(this.value)>10) this.value='10';": function (event) {
this.value=this.value.replace(/[^0-9]/g,''); if(parseInt(this.value)>10) this.value='10';
  },
  "toast('로그인 후 이용 가능합니다.')": function (event) {
toast('로그인 후 이용 가능합니다.')
  },
  "toast('모든 알림 보기')": function (event) {
toast('모든 알림 보기')
  },
  "toast('알림 이동')": function (event) {
toast('알림 이동')
  },
  "tog(this)": function (event) {
tog(this)
  },
  "tog(this);showOtherInput('other-accopts',this)": function (event) {
tog(this);showOtherInput('other-accopts',this)
  },
  "tog(this);showOtherInput('other-allergy',this,false)": function (event) {
tog(this);showOtherInput('other-allergy',this,false)
  },
  "tog(this);showOtherInput('other-food',this,false)": function (event) {
tog(this);showOtherInput('other-food',this,false)
  },
  "tog(this);showOtherInput('other-special',this)": function (event) {
tog(this);showOtherInput('other-special',this)
  },
  "tog(this);showOtherInput('other-style',this)": function (event) {
tog(this);showOtherInput('other-style',this)
  },
  "toggleInvitedDeleteMode(false)": function (event) {
toggleInvitedDeleteMode(false)
  },
  "toggleInvitedDeleteMode(true)": function (event) {
toggleInvitedDeleteMode(true)
  },
  "toggleMapLabels()": function (event) {
toggleMapLabels()
  },
  "toggleMyMenu()": function (event) {
toggleMyMenu()
  },
  "toggleMyRouteScrapDeleteMode(false)": function (event) {
toggleMyRouteScrapDeleteMode(false)
  },
  "toggleMyRouteScrapDeleteMode(true)": function (event) {
toggleMyRouteScrapDeleteMode(true)
  },
  "toggleMyTripsDeleteMode(false)": function (event) {
toggleMyTripsDeleteMode(false)
  },
  "toggleMyTripsDeleteMode(true)": function (event) {
toggleMyTripsDeleteMode(true)
  },
  "toggleOtherStay()": function (event) {
toggleOtherStay()
  },
  "togglePlanConfirm()": function (event) {
togglePlanConfirm()
  },
  "tryDemoLogin('admin')": function (event) {
tryDemoLogin('admin')
  },
  "tryDemoLogin('user')": function (event) {
tryDemoLogin('user')
  },
  "tryGoogleLogin()": function (event) {
tryGoogleLogin()
  },
  "tryKakaoLogin()": function (event) {
tryKakaoLogin()
  },
  "tryLogin()": function (event) {
tryLogin()
  },
  "updateCity(this)": function (event) {
updateCity(this)
  },
  "updateCity(this,'edit-region-city')": function (event) {
updateCity(this,'edit-region-city')
  },
  "updateCity(this,'social-region-city')": function (event) {
updateCity(this,'social-region-city')
  },
  "updateCityDep(this)": function (event) {
updateCityDep(this)
  },
  "updateCityDest(this)": function (event) {
updateCityDest(this)
  },
  "updateCurDestCity(this)": function (event) {
updateCurDestCity(this)
  },
  "verifyAuthCode()": function (event) {
verifyAuthCode()
  },
  "verifyInfoPw()": function (event) {
verifyInfoPw()
  },
  "window._budgetSelectedTripId = this.value; onWritePlanSelect(this.value)": function (event) {
window._budgetSelectedTripId = this.value; onWritePlanSelect(this.value)
  },
  "window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('blogEditor'), 'bold', '_communityWriteEditorRange')": function (event) {
window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('blogEditor'), 'bold', '_communityWriteEditorRange')
  },
  "window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('blogEditor'), 'italic', '_communityWriteEditorRange')": function (event) {
window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('blogEditor'), 'italic', '_communityWriteEditorRange')
  },
  "window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('blogEditor'), 'underline', '_communityWriteEditorRange')": function (event) {
window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('blogEditor'), 'underline', '_communityWriteEditorRange')
  },
  "window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('communityEditContent'), 'bold', '_communityEditEditorRange')": function (event) {
window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('communityEditContent'), 'bold', '_communityEditEditorRange')
  },
  "window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('communityEditContent'), 'italic', '_communityEditEditorRange')": function (event) {
window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('communityEditContent'), 'italic', '_communityEditEditorRange')
  },
  "window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('communityEditContent'), 'underline', '_communityEditEditorRange')": function (event) {
window._commUtil && window._commUtil.applyEditorCommand(document.getElementById('communityEditContent'), 'underline', '_communityEditEditorRange')
  },
  "window._commUtil && window._commUtil.saveEditorSelection(document.getElementById('blogEditor'), '_communityWriteEditorRange')": function (event) {
window._commUtil && window._commUtil.saveEditorSelection(document.getElementById('blogEditor'), '_communityWriteEditorRange')
  },
  "window._handleWriteImageSelect && window._handleWriteImageSelect(this)": function (event) {
window._handleWriteImageSelect && window._handleWriteImageSelect(this)
  },
  "window._pendingDestText='강릉';goNewPlanner()": function (event) {
window._pendingDestText='강릉';goNewPlanner()
  },
  "window._pendingDestText='경주';goNewPlanner()": function (event) {
window._pendingDestText='경주';goNewPlanner()
  },
  "window._pendingDestText='부산';goNewPlanner()": function (event) {
window._pendingDestText='부산';goNewPlanner()
  },
  "window._pendingDestText='전주';goNewPlanner()": function (event) {
window._pendingDestText='전주';goNewPlanner()
  },
  "window._pendingDestText='제주도';goNewPlanner()": function (event) {
window._pendingDestText='제주도';goNewPlanner()
  },
  "window.openWriteImagePicker && window.openWriteImagePicker()": function (event) {
window.openWriteImagePicker && window.openWriteImagePicker()
  },
};
/* 값이 끼워지는 data-on* 에서 부를 수 있는 전역 함수 (화면 코드가 선언한 것만) */
window.__inlineHandlerNames = {
  "_admAddDay": function () { return typeof _admAddDay === 'function' ? _admAddDay : undefined; },
  "_admAddItemToBlock": function () { return typeof _admAddItemToBlock === 'function' ? _admAddItemToBlock : undefined; },
  "_admAddPlanItem": function () { return typeof _admAddPlanItem === 'function' ? _admAddPlanItem : undefined; },
  "_admRemoveDay": function () { return typeof _admRemoveDay === 'function' ? _admRemoveDay : undefined; },
  "_adminBadge": function () { return typeof _adminBadge === 'function' ? _adminBadge : undefined; },
  "_adminPageInitialized": function () { return typeof _adminPageInitialized === 'function' ? _adminPageInitialized : undefined; },
  "_ah": function () { return typeof _ah === 'function' ? _ah : undefined; },
  "_aj": function () { return typeof _aj === 'function' ? _aj : undefined; },
  "_applyCurationPreferences": function () { return typeof _applyCurationPreferences === 'function' ? _applyCurationPreferences : undefined; },
  "_applyDestText": function () { return typeof _applyDestText === 'function' ? _applyDestText : undefined; },
  "_applyMainScrapStyle": function () { return typeof _applyMainScrapStyle === 'function' ? _applyMainScrapStyle : undefined; },
  "_applyPlanPrefToForm": function () { return typeof _applyPlanPrefToForm === 'function' ? _applyPlanPrefToForm : undefined; },
  "_applySort": function () { return typeof _applySort === 'function' ? _applySort : undefined; },
  "_arrowEventRegistered": function () { return typeof _arrowEventRegistered === 'function' ? _arrowEventRegistered : undefined; },
  "_budgetSnapshot": function () { return typeof _budgetSnapshot === 'function' ? _budgetSnapshot : undefined; },
  "_buildOneCarousel": function () { return typeof _buildOneCarousel === 'function' ? _buildOneCarousel : undefined; },
  "_cancelGenLoading": function () { return typeof _cancelGenLoading === 'function' ? _cancelGenLoading : undefined; },
  "_catLabel": function () { return typeof _catLabel === 'function' ? _catLabel : undefined; },
  "_chatRestored": function () { return typeof _chatRestored === 'function' ? _chatRestored : undefined; },
  "_checkIsAdmin": function () { return typeof _checkIsAdmin === 'function' ? _checkIsAdmin : undefined; },
  "_clearCurationForm": function () { return typeof _clearCurationForm === 'function' ? _clearCurationForm : undefined; },
  "_commUtil": function () { return typeof _commUtil === 'function' ? _commUtil : undefined; },
  "_communityEditEditorRange": function () { return typeof _communityEditEditorRange === 'function' ? _communityEditEditorRange : undefined; },
  "_communityEditOriginalPost": function () { return typeof _communityEditOriginalPost === 'function' ? _communityEditOriginalPost : undefined; },
  "_communityPlannerOriginalPreference": function () { return typeof _communityPlannerOriginalPreference === 'function' ? _communityPlannerOriginalPreference : undefined; },
  "_communityPostsLoadingKey": function () { return typeof _communityPostsLoadingKey === 'function' ? _communityPostsLoadingKey : undefined; },
  "_communitySelectedImages": function () { return typeof _communitySelectedImages === 'function' ? _communitySelectedImages : undefined; },
  "_communitySidePanelsLoading": function () { return typeof _communitySidePanelsLoading === 'function' ? _communitySidePanelsLoading : undefined; },
  "_communityWriteCategory": function () { return typeof _communityWriteCategory === 'function' ? _communityWriteCategory : undefined; },
  "_communityWriteImages": function () { return typeof _communityWriteImages === 'function' ? _communityWriteImages : undefined; },
  "_curationData": function () { return typeof _curationData === 'function' ? _curationData : undefined; },
  "_curationRecommendations": function () { return typeof _curationRecommendations === 'function' ? _curationRecommendations : undefined; },
  "_currentIndex": function () { return typeof _currentIndex === 'function' ? _currentIndex : undefined; },
  "_currentPlaceCategory": function () { return typeof _currentPlaceCategory === 'function' ? _currentPlaceCategory : undefined; },
  "_currentPlaceId": function () { return typeof _currentPlaceId === 'function' ? _currentPlaceId : undefined; },
  "_currentPlaceReviews": function () { return typeof _currentPlaceReviews === 'function' ? _currentPlaceReviews : undefined; },
  "_currentPlanStep": function () { return typeof _currentPlanStep === 'function' ? _currentPlanStep : undefined; },
  "_currentPostCategory": function () { return typeof _currentPostCategory === 'function' ? _currentPostCategory : undefined; },
  "_currentPostDetail": function () { return typeof _currentPostDetail === 'function' ? _currentPostDetail : undefined; },
  "_currentPostId": function () { return typeof _currentPostId === 'function' ? _currentPostId : undefined; },
  "_currentTripId": function () { return typeof _currentTripId === 'function' ? _currentTripId : undefined; },
  "_currentUser": function () { return typeof _currentUser === 'function' ? _currentUser : undefined; },
  "_dayN": function () { return typeof _dayN === 'function' ? _dayN : undefined; },
  "_demoMode": function () { return typeof _demoMode === 'function' ? _demoMode : undefined; },
  "_demoNoticeOnce": function () { return typeof _demoNoticeOnce === 'function' ? _demoNoticeOnce : undefined; },
  "_demoNoticeTimer": function () { return typeof _demoNoticeTimer === 'function' ? _demoNoticeTimer : undefined; },
  "_draw": function () { return typeof _draw === 'function' ? _draw : undefined; },
  "_drawExpensePage": function () { return typeof _drawExpensePage === 'function' ? _drawExpensePage : undefined; },
  "_drawLedgerCardPager": function () { return typeof _drawLedgerCardPager === 'function' ? _drawLedgerCardPager : undefined; },
  "_drawMyLedgerPager": function () { return typeof _drawMyLedgerPager === 'function' ? _drawMyLedgerPager : undefined; },
  "_drawPagination": function () { return typeof _drawPagination === 'function' ? _drawPagination : undefined; },
  "_esc": function () { return typeof _esc === 'function' ? _esc : undefined; },
  "_escBasis": function () { return typeof _escBasis === 'function' ? _escBasis : undefined; },
  "_escSafe": function () { return typeof _escSafe === 'function' ? _escSafe : undefined; },
  "_finalizeMap": function () { return typeof _finalizeMap === 'function' ? _finalizeMap : undefined; },
  "_finalizePlannerRoute": function () { return typeof _finalizePlannerRoute === 'function' ? _finalizePlannerRoute : undefined; },
  "_fmt": function () { return typeof _fmt === 'function' ? _fmt : undefined; },
  "_fmtWon": function () { return typeof _fmtWon === 'function' ? _fmtWon : undefined; },
  "_genCancelled": function () { return typeof _genCancelled === 'function' ? _genCancelled : undefined; },
  "_geocodeAndDrawPreview": function () { return typeof _geocodeAndDrawPreview === 'function' ? _geocodeAndDrawPreview : undefined; },
  "_getBudgetPagerWindow": function () { return typeof _getBudgetPagerWindow === 'function' ? _getBudgetPagerWindow : undefined; },
  "_getMyPageWindow": function () { return typeof _getMyPageWindow === 'function' ? _getMyPageWindow : undefined; },
  "_goReportPage": function () { return typeof _goReportPage === 'function' ? _goReportPage : undefined; },
  "_goToUrl": function () { return typeof _goToUrl === 'function' ? _goToUrl : undefined; },
  "_goUserPage": function () { return typeof _goUserPage === 'function' ? _goUserPage : undefined; },
  "_handleOAuthCallback": function () { return typeof _handleOAuthCallback === 'function' ? _handleOAuthCallback : undefined; },
  "_handleWriteImageSelect": function () { return typeof _handleWriteImageSelect === 'function' ? _handleWriteImageSelect : undefined; },
  "_hasPlannerDraft": function () { return typeof _hasPlannerDraft === 'function' ? _hasPlannerDraft : undefined; },
  "_haversineKm": function () { return typeof _haversineKm === 'function' ? _haversineKm : undefined; },
  "_hideGenLoading": function () { return typeof _hideGenLoading === 'function' ? _hideGenLoading : undefined; },
  "_hookAdminGo": function () { return typeof _hookAdminGo === 'function' ? _hookAdminGo : undefined; },
  "_hookGo": function () { return typeof _hookGo === 'function' ? _hookGo : undefined; },
  "_initSession": function () { return typeof _initSession === 'function' ? _initSession : undefined; },
  "_initStep1AutoSave": function () { return typeof _initStep1AutoSave === 'function' ? _initStep1AutoSave : undefined; },
  "_invitedDeleteMode": function () { return typeof _invitedDeleteMode === 'function' ? _invitedDeleteMode : undefined; },
  "_invitedTripsCurrentPage": function () { return typeof _invitedTripsCurrentPage === 'function' ? _invitedTripsCurrentPage : undefined; },
  "_invitedTripsData": function () { return typeof _invitedTripsData === 'function' ? _invitedTripsData : undefined; },
  "_isAdmin": function () { return typeof _isAdmin === 'function' ? _isAdmin : undefined; },
  "_isInvitedEditView": function () { return typeof _isInvitedEditView === 'function' ? _isInvitedEditView : undefined; },
  "_isKakaoMapInitRunning": function () { return typeof _isKakaoMapInitRunning === 'function' ? _isKakaoMapInitRunning : undefined; },
  "_isSavingAndLeaving": function () { return typeof _isSavingAndLeaving === 'function' ? _isSavingAndLeaving : undefined; },
  "_kakaoArrows": function () { return typeof _kakaoArrows === 'function' ? _kakaoArrows : undefined; },
  "_kakaoBounds": function () { return typeof _kakaoBounds === 'function' ? _kakaoBounds : undefined; },
  "_kakaoMap": function () { return typeof _kakaoMap === 'function' ? _kakaoMap : undefined; },
  "_kakaoOverlays": function () { return typeof _kakaoOverlays === 'function' ? _kakaoOverlays : undefined; },
  "_kakaoPolylines": function () { return typeof _kakaoPolylines === 'function' ? _kakaoPolylines : undefined; },
  "_lastCreatedPostId": function () { return typeof _lastCreatedPostId === 'function' ? _lastCreatedPostId : undefined; },
  "_loadBudgetBasis": function () { return typeof _loadBudgetBasis === 'function' ? _loadBudgetBasis : undefined; },
  "_loadExpenses": function () { return typeof _loadExpenses === 'function' ? _loadExpenses : undefined; },
  "_loadMapBudget": function () { return typeof _loadMapBudget === 'function' ? _loadMapBudget : undefined; },
  "_loadMyScrappedPlaceIds": function () { return typeof _loadMyScrappedPlaceIds === 'function' ? _loadMyScrappedPlaceIds : undefined; },
  "_loadNotifications": function () { return typeof _loadNotifications === 'function' ? _loadNotifications : undefined; },
  "_loadPlaceCards": function () { return typeof _loadPlaceCards === 'function' ? _loadPlaceCards : undefined; },
  "_mainIsLoggedIn": function () { return typeof _mainIsLoggedIn === 'function' ? _mainIsLoggedIn : undefined; },
  "_mapDestRegion": function () { return typeof _mapDestRegion === 'function' ? _mapDestRegion : undefined; },
  "_mapLabelsOn": function () { return typeof _mapLabelsOn === 'function' ? _mapLabelsOn : undefined; },
  "_maskEmail": function () { return typeof _maskEmail === 'function' ? _maskEmail : undefined; },
  "_mjs": function () { return typeof _mjs === 'function' ? _mjs : undefined; },
  "_mobCarouselResizeT": function () { return typeof _mobCarouselResizeT === 'function' ? _mobCarouselResizeT : undefined; },
  "_mpOpenReview": function () { return typeof _mpOpenReview === 'function' ? _mpOpenReview : undefined; },
  "_mx": function () { return typeof _mx === 'function' ? _mx : undefined; },
  "_myCommunityPagingState": function () { return typeof _myCommunityPagingState === 'function' ? _myCommunityPagingState : undefined; },
  "_myRouteScrapDeleteMode": function () { return typeof _myRouteScrapDeleteMode === 'function' ? _myRouteScrapDeleteMode : undefined; },
  "_myTripsCurrentPage": function () { return typeof _myTripsCurrentPage === 'function' ? _myTripsCurrentPage : undefined; },
  "_myTripsData": function () { return typeof _myTripsData === 'function' ? _myTripsData : undefined; },
  "_myTripsDeleteMode": function () { return typeof _myTripsDeleteMode === 'function' ? _myTripsDeleteMode : undefined; },
  "_myUserId": function () { return typeof _myUserId === 'function' ? _myUserId : undefined; },
  "_onMobMQChange": function () { return typeof _onMobMQChange === 'function' ? _onMobMQChange : undefined; },
  "_onNotFound": function () { return typeof _onNotFound === 'function' ? _onNotFound : undefined; },
  "_onResolved": function () { return typeof _onResolved === 'function' ? _onResolved : undefined; },
  "_openPlaceReviews": function () { return typeof _openPlaceReviews === 'function' ? _openPlaceReviews : undefined; },
  "_openedPostId": function () { return typeof _openedPostId === 'function' ? _openedPostId : undefined; },
  "_pendingAdminSection": function () { return typeof _pendingAdminSection === 'function' ? _pendingAdminSection : undefined; },
  "_pendingCommunityDest": function () { return typeof _pendingCommunityDest === 'function' ? _pendingCommunityDest : undefined; },
  "_pendingCurationPlan": function () { return typeof _pendingCurationPlan === 'function' ? _pendingCurationPlan : undefined; },
  "_pendingDestText": function () { return typeof _pendingDestText === 'function' ? _pendingDestText : undefined; },
  "_pendingLoginThenPlanner": function () { return typeof _pendingLoginThenPlanner === 'function' ? _pendingLoginThenPlanner : undefined; },
  "_pendingPlaceReview": function () { return typeof _pendingPlaceReview === 'function' ? _pendingPlaceReview : undefined; },
  "_pickInRegion": function () { return typeof _pickInRegion === 'function' ? _pickInRegion : undefined; },
  "_pinMapLabel": function () { return typeof _pinMapLabel === 'function' ? _pinMapLabel : undefined; },
  "_pinnedLabelKey": function () { return typeof _pinnedLabelKey === 'function' ? _pinnedLabelKey : undefined; },
  "_placeCatIcon": function () { return typeof _placeCatIcon === 'function' ? _placeCatIcon : undefined; },
  "_placeCatLabel": function () { return typeof _placeCatLabel === 'function' ? _placeCatLabel : undefined; },
  "_placeSnapshotClick": function () { return typeof _placeSnapshotClick === 'function' ? _placeSnapshotClick : undefined; },
  "_placeTabPageState": function () { return typeof _placeTabPageState === 'function' ? _placeTabPageState : undefined; },
  "_planConfirmed": function () { return typeof _planConfirmed === 'function' ? _planConfirmed : undefined; },
  "_planDirty": function () { return typeof _planDirty === 'function' ? _planDirty : undefined; },
  "_planHydrateTripId": function () { return typeof _planHydrateTripId === 'function' ? _planHydrateTripId : undefined; },
  "_planLoadedTripId": function () { return typeof _planLoadedTripId === 'function' ? _planLoadedTripId : undefined; },
  "_popCard": function () { return typeof _popCard === 'function' ? _popCard : undefined; },
  "_populateLedgerTripCards": function () { return typeof _populateLedgerTripCards === 'function' ? _populateLedgerTripCards : undefined; },
  "_preGenerateAudit": function () { return typeof _preGenerateAudit === 'function' ? _preGenerateAudit : undefined; },
  "_q": function () { return typeof _q === 'function' ? _q : undefined; },
  "_rChips": function () { return typeof _rChips === 'function' ? _rChips : undefined; },
  "_rSort": function () { return typeof _rSort === 'function' ? _rSort : undefined; },
  "_reRenderUserTable": function () { return typeof _reRenderUserTable === 'function' ? _reRenderUserTable : undefined; },
  "_rectsHit": function () { return typeof _rectsHit === 'function' ? _rectsHit : undefined; },
  "_removeOpenClass": function () { return typeof _removeOpenClass === 'function' ? _removeOpenClass : undefined; },
  "_renderBudgetBasis": function () { return typeof _renderBudgetBasis === 'function' ? _renderBudgetBasis : undefined; },
  "_renderItinerary": function () { return typeof _renderItinerary === 'function' ? _renderItinerary : undefined; },
  "_renderMyInvitedTrips": function () { return typeof _renderMyInvitedTrips === 'function' ? _renderMyInvitedTrips : undefined; },
  "_renderMyLikedPosts": function () { return typeof _renderMyLikedPosts === 'function' ? _renderMyLikedPosts : undefined; },
  "_renderMyPagePagerHtml": function () { return typeof _renderMyPagePagerHtml === 'function' ? _renderMyPagePagerHtml : undefined; },
  "_renderMyReviews": function () { return typeof _renderMyReviews === 'function' ? _renderMyReviews : undefined; },
  "_renderMyTrips": function () { return typeof _renderMyTrips === 'function' ? _renderMyTrips : undefined; },
  "_renderPagination": function () { return typeof _renderPagination === 'function' ? _renderPagination : undefined; },
  "_renderPostList": function () { return typeof _renderPostList === 'function' ? _renderPostList : undefined; },
  "_reorderRouteByProximity": function () { return typeof _reorderRouteByProximity === 'function' ? _reorderRouteByProximity : undefined; },
  "_reportPostId": function () { return typeof _reportPostId === 'function' ? _reportPostId : undefined; },
  "_restoreChatForTrip": function () { return typeof _restoreChatForTrip === 'function' ? _restoreChatForTrip : undefined; },
  "_restorePlannerDraft": function () { return typeof _restorePlannerDraft === 'function' ? _restorePlannerDraft : undefined; },
  "_restoringPlan": function () { return typeof _restoringPlan === 'function' ? _restoringPlan : undefined; },
  "_runSearchLoopAfterCenter": function () { return typeof _runSearchLoopAfterCenter === 'function' ? _runSearchLoopAfterCenter : undefined; },
  "_runWithRegion": function () { return typeof _runWithRegion === 'function' ? _runWithRegion : undefined; },
  "_savePlannerDraft": function () { return typeof _savePlannerDraft === 'function' ? _savePlannerDraft : undefined; },
  "_scrapCatIcon": function () { return typeof _scrapCatIcon === 'function' ? _scrapCatIcon : undefined; },
  "_scrapDbCategory": function () { return typeof _scrapDbCategory === 'function' ? _scrapDbCategory : undefined; },
  "_scrappedPlaceIds": function () { return typeof _scrappedPlaceIds === 'function' ? _scrappedPlaceIds : undefined; },
  "_scrappedPostIds": function () { return typeof _scrappedPostIds === 'function' ? _scrappedPostIds : undefined; },
  "_scrollToCard": function () { return typeof _scrollToCard === 'function' ? _scrollToCard : undefined; },
  "_selLedgerCard": function () { return typeof _selLedgerCard === 'function' ? _selLedgerCard : undefined; },
  "_sendQuickReply": function () { return typeof _sendQuickReply === 'function' ? _sendQuickReply : undefined; },
  "_setHTML": function () { return typeof _setHTML === 'function' ? _setHTML : undefined; },
  "_setLedgerCardPage": function () { return typeof _setLedgerCardPage === 'function' ? _setLedgerCardPage : undefined; },
  "_setMyLedgerPage": function () { return typeof _setMyLedgerPage === 'function' ? _setMyLedgerPage : undefined; },
  "_setText": function () { return typeof _setText === 'function' ? _setText : undefined; },
  "_showGenLoading": function () { return typeof _showGenLoading === 'function' ? _showGenLoading : undefined; },
  "_showOver50Gate": function () { return typeof _showOver50Gate === 'function' ? _showOver50Gate : undefined; },
  "_showPreGenGate": function () { return typeof _showPreGenGate === 'function' ? _showPreGenGate : undefined; },
  "_slideAutoTimer": function () { return typeof _slideAutoTimer === 'function' ? _slideAutoTimer : undefined; },
  "_sortPlaceReviews": function () { return typeof _sortPlaceReviews === 'function' ? _sortPlaceReviews : undefined; },
  "_startLockCountdown": function () { return typeof _startLockCountdown === 'function' ? _startLockCountdown : undefined; },
  "_startPinSearch": function () { return typeof _startPinSearch === 'function' ? _startPinSearch : undefined; },
  "_startPinSearchLoop": function () { return typeof _startPinSearchLoop === 'function' ? _startPinSearchLoop : undefined; },
  "_stayBookingInfo": function () { return typeof _stayBookingInfo === 'function' ? _stayBookingInfo : undefined; },
  "_step1AutoSaveInitialized": function () { return typeof _step1AutoSaveInitialized === 'function' ? _step1AutoSaveInitialized : undefined; },
  "_sync": function () { return typeof _sync === 'function' ? _sync : undefined; },
  "_syncNavStepHighlight": function () { return typeof _syncNavStepHighlight === 'function' ? _syncNavStepHighlight : undefined; },
  "_syncPlannerTopbar": function () { return typeof _syncPlannerTopbar === 'function' ? _syncPlannerTopbar : undefined; },
  "_syncStep4State": function () { return typeof _syncStep4State === 'function' ? _syncStep4State : undefined; },
  "_teardownCarousels": function () { return typeof _teardownCarousels === 'function' ? _teardownCarousels : undefined; },
  "_teardownOneCarousel": function () { return typeof _teardownOneCarousel === 'function' ? _teardownOneCarousel : undefined; },
  "_toggleCheckbox": function () { return typeof _toggleCheckbox === 'function' ? _toggleCheckbox : undefined; },
  "_uSort": function () { return typeof _uSort === 'function' ? _uSort : undefined; },
  "_updateExportButtons": function () { return typeof _updateExportButtons === 'function' ? _updateExportButtons : undefined; },
  "_updateScrapCount": function () { return typeof _updateScrapCount === 'function' ? _updateScrapCount : undefined; },
  "_validatePlanStep1": function () { return typeof _validatePlanStep1 === 'function' ? _validatePlanStep1 : undefined; },
  "_validatePlanStep2": function () { return typeof _validatePlanStep2 === 'function' ? _validatePlanStep2 : undefined; },
  "_watchCarouselGrids": function () { return typeof _watchCarouselGrids === 'function' ? _watchCarouselGrids : undefined; },
  "addBubble": function () { return typeof addBubble === 'function' ? addBubble : undefined; },
  "addDay": function () { return typeof addDay === 'function' ? addDay : undefined; },
  "addLedgerExpense": function () { return typeof addLedgerExpense === 'function' ? addLedgerExpense : undefined; },
  "addPlanItem": function () { return typeof addPlanItem === 'function' ? addPlanItem : undefined; },
  "addQueue": function () { return typeof addQueue === 'function' ? addQueue : undefined; },
  "apiCall": function () { return typeof apiCall === 'function' ? apiCall : undefined; },
  "applyActionState": function () { return typeof applyActionState === 'function' ? applyActionState : undefined; },
  "applyCommunityDestinationToPlanner": function () { return typeof applyCommunityDestinationToPlanner === 'function' ? applyCommunityDestinationToPlanner : undefined; },
  "applyCommunityPlannerSeedToPlanner": function () { return typeof applyCommunityPlannerSeedToPlanner === 'function' ? applyCommunityPlannerSeedToPlanner : undefined; },
  "applyEditorCommand": function () { return typeof applyEditorCommand === 'function' ? applyEditorCommand : undefined; },
  "applyPlanConfirmState": function () { return typeof applyPlanConfirmState === 'function' ? applyPlanConfirmState : undefined; },
  "applyTagFilter": function () { return typeof applyTagFilter === 'function' ? applyTagFilter : undefined; },
  "authHeaders": function () { return typeof authHeaders === 'function' ? authHeaders : undefined; },
  "beautifyReviewDetail": function () { return typeof beautifyReviewDetail === 'function' ? beautifyReviewDetail : undefined; },
  "bindCommunityImageButton": function () { return typeof bindCommunityImageButton === 'function' ? bindCommunityImageButton : undefined; },
  "bindEditorSelectionMemory": function () { return typeof bindEditorSelectionMemory === 'function' ? bindEditorSelectionMemory : undefined; },
  "bindInlineImageDeleteSupport": function () { return typeof bindInlineImageDeleteSupport === 'function' ? bindInlineImageDeleteSupport : undefined; },
  "buildCommunityPlannerSeed": function () { return typeof buildCommunityPlannerSeed === 'function' ? buildCommunityPlannerSeed : undefined; },
  "buildEditHTML": function () { return typeof buildEditHTML === 'function' ? buildEditHTML : undefined; },
  "buildEditableContentWithImages": function () { return typeof buildEditableContentWithImages === 'function' ? buildEditableContentWithImages : undefined; },
  "buildGridHTML": function () { return typeof buildGridHTML === 'function' ? buildGridHTML : undefined; },
  "calculatePlaceTotalPages": function () { return typeof calculatePlaceTotalPages === 'function' ? calculatePlaceTotalPages : undefined; },
  "cancelRouteScrap": function () { return typeof cancelRouteScrap === 'function' ? cancelRouteScrap : undefined; },
  "changeRole": function () { return typeof changeRole === 'function' ? changeRole : undefined; },
  "changeSlide": function () { return typeof changeSlide === 'function' ? changeSlide : undefined; },
  "checkActualWeatherAlert": function () { return typeof checkActualWeatherAlert === 'function' ? checkActualWeatherAlert : undefined; },
  "checkAndOpenWrite": function () { return typeof checkAndOpenWrite === 'function' ? checkAndOpenWrite : undefined; },
  "checkDuplicateId": function () { return typeof checkDuplicateId === 'function' ? checkDuplicateId : undefined; },
  "checkEmail": function () { return typeof checkEmail === 'function' ? checkEmail : undefined; },
  "checkPw": function () { return typeof checkPw === 'function' ? checkPw : undefined; },
  "checkPw2": function () { return typeof checkPw2 === 'function' ? checkPw2 : undefined; },
  "checkUname": function () { return typeof checkUname === 'function' ? checkUname : undefined; },
  "checkUnsavedChangesAndSave": function () { return typeof checkUnsavedChangesAndSave === 'function' ? checkUnsavedChangesAndSave : undefined; },
  "clean": function () { return typeof clean === 'function' ? clean : undefined; },
  "cleanTag": function () { return typeof cleanTag === 'function' ? cleanTag : undefined; },
  "cleanupDeletedInlineImageRecords": function () { return typeof cleanupDeletedInlineImageRecords === 'function' ? cleanupDeletedInlineImageRecords : undefined; },
  "clearExtraRow": function () { return typeof clearExtraRow === 'function' ? clearExtraRow : undefined; },
  "clearInlineImageSelection": function () { return typeof clearInlineImageSelection === 'function' ? clearInlineImageSelection : undefined; },
  "clearOtherInputs": function () { return typeof clearOtherInputs === 'function' ? clearOtherInputs : undefined; },
  "clearSumField": function () { return typeof clearSumField === 'function' ? clearSumField : undefined; },
  "cloneCommunityPlannerSeed": function () { return typeof cloneCommunityPlannerSeed === 'function' ? cloneCommunityPlannerSeed : undefined; },
  "closeCommunityPlanPreview": function () { return typeof closeCommunityPlanPreview === 'function' ? closeCommunityPlanPreview : undefined; },
  "closeEditCommentModal": function () { return typeof closeEditCommentModal === 'function' ? closeEditCommentModal : undefined; },
  "closeEditModal": function () { return typeof closeEditModal === 'function' ? closeEditModal : undefined; },
  "closeNotifPopup": function () { return typeof closeNotifPopup === 'function' ? closeNotifPopup : undefined; },
  "closePrev": function () { return typeof closePrev === 'function' ? closePrev : undefined; },
  "closePriceModal": function () { return typeof closePriceModal === 'function' ? closePriceModal : undefined; },
  "closePwChangeRecommendModal": function () { return typeof closePwChangeRecommendModal === 'function' ? closePwChangeRecommendModal : undefined; },
  "closePwReset": function () { return typeof closePwReset === 'function' ? closePwReset : undefined; },
  "closePwResetModal": function () { return typeof closePwResetModal === 'function' ? closePwResetModal : undefined; },
  "closeQueue": function () { return typeof closeQueue === 'function' ? closeQueue : undefined; },
  "closeReportAction": function () { return typeof closeReportAction === 'function' ? closeReportAction : undefined; },
  "closeSiteModal": function () { return typeof closeSiteModal === 'function' ? closeSiteModal : undefined; },
  "closeSuspendModal": function () { return typeof closeSuspendModal === 'function' ? closeSuspendModal : undefined; },
  "closeSuspendedAlert": function () { return typeof closeSuspendedAlert === 'function' ? closeSuspendedAlert : undefined; },
  "closeUnsavedChangesModal": function () { return typeof closeUnsavedChangesModal === 'function' ? closeUnsavedChangesModal : undefined; },
  "closeWeatherPopup": function () { return typeof closeWeatherPopup === 'function' ? closeWeatherPopup : undefined; },
  "closeWithdrawModal": function () { return typeof closeWithdrawModal === 'function' ? closeWithdrawModal : undefined; },
  "closeWrite": function () { return typeof closeWrite === 'function' ? closeWrite : undefined; },
  "communityV2Search": function () { return typeof communityV2Search === 'function' ? communityV2Search : undefined; },
  "confirmLeaveAndSave": function () { return typeof confirmLeaveAndSave === 'function' ? confirmLeaveAndSave : undefined; },
  "confirmReportAction": function () { return typeof confirmReportAction === 'function' ? confirmReportAction : undefined; },
  "confirmSuspend": function () { return typeof confirmSuspend === 'function' ? confirmSuspend : undefined; },
  "confirmWithdraw": function () { return typeof confirmWithdraw === 'function' ? confirmWithdraw : undefined; },
  "copyShareLink": function () { return typeof copyShareLink === 'function' ? copyShareLink : undefined; },
  "copyToClipboard": function () { return typeof copyToClipboard === 'function' ? copyToClipboard : undefined; },
  "createPlan": function () { return typeof createPlan === 'function' ? createPlan : undefined; },
  "deleteCuration": function () { return typeof deleteCuration === 'function' ? deleteCuration : undefined; },
  "deleteCurrentPostFromDetail": function () { return typeof deleteCurrentPostFromDetail === 'function' ? deleteCurrentPostFromDetail : undefined; },
  "deleteExpense": function () { return typeof deleteExpense === 'function' ? deleteExpense : undefined; },
  "deleteMyComment": function () { return typeof deleteMyComment === 'function' ? deleteMyComment : undefined; },
  "deleteMyPlaceScrap": function () { return typeof deleteMyPlaceScrap === 'function' ? deleteMyPlaceScrap : undefined; },
  "deleteMyPost": function () { return typeof deleteMyPost === 'function' ? deleteMyPost : undefined; },
  "deleteMyRouteScrap": function () { return typeof deleteMyRouteScrap === 'function' ? deleteMyRouteScrap : undefined; },
  "deleteNotif": function () { return typeof deleteNotif === 'function' ? deleteNotif : undefined; },
  "deleteScrap": function () { return typeof deleteScrap === 'function' ? deleteScrap : undefined; },
  "demoBlockReplace": function () { return typeof demoBlockReplace === 'function' ? demoBlockReplace : undefined; },
  "doCommPlaceScrap": function () { return typeof doCommPlaceScrap === 'function' ? doCommPlaceScrap : undefined; },
  "doCommPlaceScrapToggle": function () { return typeof doCommPlaceScrapToggle === 'function' ? doCommPlaceScrapToggle : undefined; },
  "doCommentWrite": function () { return typeof doCommentWrite === 'function' ? doCommentWrite : undefined; },
  "doConfirmPlan": function () { return typeof doConfirmPlan === 'function' ? doConfirmPlan : undefined; },
  "doEditReviewSave": function () { return typeof doEditReviewSave === 'function' ? doEditReviewSave : undefined; },
  "doLogout": function () { return typeof doLogout === 'function' ? doLogout : undefined; },
  "doPlaceScrap": function () { return typeof doPlaceScrap === 'function' ? doPlaceScrap : undefined; },
  "doPreviewScrap": function () { return typeof doPreviewScrap === 'function' ? doPreviewScrap : undefined; },
  "doReviewLike": function () { return typeof doReviewLike === 'function' ? doReviewLike : undefined; },
  "doReviewReport": function () { return typeof doReviewReport === 'function' ? doReviewReport : undefined; },
  "doReviewScrap": function () { return typeof doReviewScrap === 'function' ? doReviewScrap : undefined; },
  "doSearch": function () { return typeof doSearch === 'function' ? doSearch : undefined; },
  "doShareInvite": function () { return typeof doShareInvite === 'function' ? doShareInvite : undefined; },
  "doShareLinkCopy": function () { return typeof doShareLinkCopy === 'function' ? doShareLinkCopy : undefined; },
  "doSignup": function () { return typeof doSignup === 'function' ? doSignup : undefined; },
  "doSocialSignup": function () { return typeof doSocialSignup === 'function' ? doSocialSignup : undefined; },
  "doWithdraw": function () { return typeof doWithdraw === 'function' ? doWithdraw : undefined; },
  "drawLivePolylines": function () { return typeof drawLivePolylines === 'function' ? drawLivePolylines : undefined; },
  "editMyPost": function () { return typeof editMyPost === 'function' ? editMyPost : undefined; },
  "editorHasContent": function () { return typeof editorHasContent === 'function' ? editorHasContent : undefined; },
  "emptyMyPageMessage": function () { return typeof emptyMyPageMessage === 'function' ? emptyMyPageMessage : undefined; },
  "ensureCommentEditModal": function () { return typeof ensureCommentEditModal === 'function' ? ensureCommentEditModal : undefined; },
  "ensureEditModal": function () { return typeof ensureEditModal === 'function' ? ensureEditModal : undefined; },
  "ensureImageInput": function () { return typeof ensureImageInput === 'function' ? ensureImageInput : undefined; },
  "ensurePreviewModal": function () { return typeof ensurePreviewModal === 'function' ? ensurePreviewModal : undefined; },
  "esc": function () { return typeof esc === 'function' ? esc : undefined; },
  "escapeHtml": function () { return typeof escapeHtml === 'function' ? escapeHtml : undefined; },
  "escapeHtmlBubble": function () { return typeof escapeHtmlBubble === 'function' ? escapeHtmlBubble : undefined; },
  "escapeJsString": function () { return typeof escapeJsString === 'function' ? escapeJsString : undefined; },
  "execAllReplace": function () { return typeof execAllReplace === 'function' ? execAllReplace : undefined; },
  "execInvitedBulkDelete": function () { return typeof execInvitedBulkDelete === 'function' ? execInvitedBulkDelete : undefined; },
  "execMyRouteScrapBulkDelete": function () { return typeof execMyRouteScrapBulkDelete === 'function' ? execMyRouteScrapBulkDelete : undefined; },
  "execMyTripsBulkDelete": function () { return typeof execMyTripsBulkDelete === 'function' ? execMyTripsBulkDelete : undefined; },
  "exportBudgetCSV": function () { return typeof exportBudgetCSV === 'function' ? exportBudgetCSV : undefined; },
  "exportBudgetPDF": function () { return typeof exportBudgetPDF === 'function' ? exportBudgetPDF : undefined; },
  "extractAmount": function () { return typeof extractAmount === 'function' ? extractAmount : undefined; },
  "extractArray": function () { return typeof extractArray === 'function' ? extractArray : undefined; },
  "extractImageUrlsFromHtml": function () { return typeof extractImageUrlsFromHtml === 'function' ? extractImageUrlsFromHtml : undefined; },
  "extractList": function () { return typeof extractList === 'function' ? extractList : undefined; },
  "extractPage": function () { return typeof extractPage === 'function' ? extractPage : undefined; },
  "extractPosts": function () { return typeof extractPosts === 'function' ? extractPosts : undefined; },
  "extractPreviewScrapList": function () { return typeof extractPreviewScrapList === 'function' ? extractPreviewScrapList : undefined; },
  "extractTotalPages": function () { return typeof extractTotalPages === 'function' ? extractTotalPages : undefined; },
  "fetchAllPlaceCards": function () { return typeof fetchAllPlaceCards === 'function' ? fetchAllPlaceCards : undefined; },
  "fetchAllRoutePosts": function () { return typeof fetchAllRoutePosts === 'function' ? fetchAllRoutePosts : undefined; },
  "fetchCommunityPostsForSide": function () { return typeof fetchCommunityPostsForSide === 'function' ? fetchCommunityPostsForSide : undefined; },
  "fetchPlacePosts": function () { return typeof fetchPlacePosts === 'function' ? fetchPlacePosts : undefined; },
  "fetchPlaceReviews": function () { return typeof fetchPlaceReviews === 'function' ? fetchPlaceReviews : undefined; },
  "fetchShareLink": function () { return typeof fetchShareLink === 'function' ? fetchShareLink : undefined; },
  "filterAdminUserRows": function () { return typeof filterAdminUserRows === 'function' ? filterAdminUserRows : undefined; },
  "filterByTag": function () { return typeof filterByTag === 'function' ? filterByTag : undefined; },
  "filterPlaceCards": function () { return typeof filterPlaceCards === 'function' ? filterPlaceCards : undefined; },
  "filterRoutePosts": function () { return typeof filterRoutePosts === 'function' ? filterRoutePosts : undefined; },
  "finalize": function () { return typeof finalize === 'function' ? finalize : undefined; },
  "finalizeInlineEditorImages": function () { return typeof finalizeInlineEditorImages === 'function' ? finalizeInlineEditorImages : undefined; },
  "finalizeSearch": function () { return typeof finalizeSearch === 'function' ? finalizeSearch : undefined; },
  "findActionButton": function () { return typeof findActionButton === 'function' ? findActionButton : undefined; },
  "findAdjacentImageForDelete": function () { return typeof findAdjacentImageForDelete === 'function' ? findAdjacentImageForDelete : undefined; },
  "findCurrentCommentById": function () { return typeof findCurrentCommentById === 'function' ? findCurrentCommentById : undefined; },
  "findImageButton": function () { return typeof findImageButton === 'function' ? findImageButton : undefined; },
  "findImgFrom": function () { return typeof findImgFrom === 'function' ? findImgFrom : undefined; },
  "findReportedAuthor": function () { return typeof findReportedAuthor === 'function' ? findReportedAuthor : undefined; },
  "findStayByKey": function () { return typeof findStayByKey === 'function' ? findStayByKey : undefined; },
  "fitMapToVisibleRoute": function () { return typeof fitMapToVisibleRoute === 'function' ? fitMapToVisibleRoute : undefined; },
  "fixReviewCtaByCategory": function () { return typeof fixReviewCtaByCategory === 'function' ? fixReviewCtaByCategory : undefined; },
  "focusPlaceRow": function () { return typeof focusPlaceRow === 'function' ? focusPlaceRow : undefined; },
  "forceLogout": function () { return typeof forceLogout === 'function' ? forceLogout : undefined; },
  "formatBotReply": function () { return typeof formatBotReply === 'function' ? formatBotReply : undefined; },
  "formatBudget": function () { return typeof formatBudget === 'function' ? formatBudget : undefined; },
  "formatDate": function () { return typeof formatDate === 'function' ? formatDate : undefined; },
  "formatMyRouteScrapDate": function () { return typeof formatMyRouteScrapDate === 'function' ? formatMyRouteScrapDate : undefined; },
  "formatNotifContent": function () { return typeof formatNotifContent === 'function' ? formatNotifContent : undefined; },
  "formatPostDate": function () { return typeof formatPostDate === 'function' ? formatPostDate : undefined; },
  "generateScheduleAndGoMap": function () { return typeof generateScheduleAndGoMap === 'function' ? generateScheduleAndGoMap : undefined; },
  "getAccessToken": function () { return typeof getAccessToken === 'function' ? getAccessToken : undefined; },
  "getAllPlaces": function () { return typeof getAllPlaces === 'function' ? getAllPlaces : undefined; },
  "getCategoryClass": function () { return typeof getCategoryClass === 'function' ? getCategoryClass : undefined; },
  "getCategoryFromCurrentTabButton": function () { return typeof getCategoryFromCurrentTabButton === 'function' ? getCategoryFromCurrentTabButton : undefined; },
  "getCategoryLabel": function () { return typeof getCategoryLabel === 'function' ? getCategoryLabel : undefined; },
  "getCommunityPlannerApiData": function () { return typeof getCommunityPlannerApiData === 'function' ? getCommunityPlannerApiData : undefined; },
  "getCoord": function () { return typeof getCoord === 'function' ? getCoord : undefined; },
  "getCurrentPostId": function () { return typeof getCurrentPostId === 'function' ? getCurrentPostId : undefined; },
  "getCurrentTabKey": function () { return typeof getCurrentTabKey === 'function' ? getCurrentTabKey : undefined; },
  "getCurrentTabKeyForSearch": function () { return typeof getCurrentTabKeyForSearch === 'function' ? getCurrentTabKeyForSearch : undefined; },
  "getDayColor": function () { return typeof getDayColor === 'function' ? getDayColor : undefined; },
  "getDayCount": function () { return typeof getDayCount === 'function' ? getDayCount : undefined; },
  "getEditorCommandState": function () { return typeof getEditorCommandState === 'function' ? getEditorCommandState : undefined; },
  "getLoggedInUserIdForReviewEdit": function () { return typeof getLoggedInUserIdForReviewEdit === 'function' ? getLoggedInUserIdForReviewEdit : undefined; },
  "getMapLinks": function () { return typeof getMapLinks === 'function' ? getMapLinks : undefined; },
  "getMyCommunityPagingState": function () { return typeof getMyCommunityPagingState === 'function' ? getMyCommunityPagingState : undefined; },
  "getPagerWindow": function () { return typeof getPagerWindow === 'function' ? getPagerWindow : undefined; },
  "getPinColor": function () { return typeof getPinColor === 'function' ? getPinColor : undefined; },
  "getPlaceId": function () { return typeof getPlaceId === 'function' ? getPlaceId : undefined; },
  "getPlaceName": function () { return typeof getPlaceName === 'function' ? getPlaceName : undefined; },
  "getPlacePageState": function () { return typeof getPlacePageState === 'function' ? getPlacePageState : undefined; },
  "getPlaceShortType": function () { return typeof getPlaceShortType === 'function' ? getPlaceShortType : undefined; },
  "getPlaceSortValue": function () { return typeof getPlaceSortValue === 'function' ? getPlaceSortValue : undefined; },
  "getPlanSummary": function () { return typeof getPlanSummary === 'function' ? getPlanSummary : undefined; },
  "getPostDateValue": function () { return typeof getPostDateValue === 'function' ? getPostDateValue : undefined; },
  "getPostNumber": function () { return typeof getPostNumber === 'function' ? getPostNumber : undefined; },
  "getPostScore": function () { return typeof getPostScore === 'function' ? getPostScore : undefined; },
  "getPreviewScrapPostId": function () { return typeof getPreviewScrapPostId === 'function' ? getPreviewScrapPostId : undefined; },
  "getPreviewScrapStateFromPost": function () { return typeof getPreviewScrapStateFromPost === 'function' ? getPreviewScrapStateFromPost : undefined; },
  "getRouteData": function () { return typeof getRouteData === 'function' ? getRouteData : undefined; },
  "getSearchKeyword": function () { return typeof getSearchKeyword === 'function' ? getSearchKeyword : undefined; },
  "getSearchType": function () { return typeof getSearchType === 'function' ? getSearchType : undefined; },
  "getSortValue": function () { return typeof getSortValue === 'function' ? getSortValue : undefined; },
  "getTripId": function () { return typeof getTripId === 'function' ? getTripId : undefined; },
  "getTripTitle": function () { return typeof getTripTitle === 'function' ? getTripTitle : undefined; },
  "go": function () { return typeof go === 'function' ? go : undefined; },
  "goAdminRefresh": function () { return typeof goAdminRefresh === 'function' ? goAdminRefresh : undefined; },
  "goChangePw": function () { return typeof goChangePw === 'function' ? goChangePw : undefined; },
  "goEditReview": function () { return typeof goEditReview === 'function' ? goEditReview : undefined; },
  "goHomeRefresh": function () { return typeof goHomeRefresh === 'function' ? goHomeRefresh : undefined; },
  "goLedger2": function () { return typeof goLedger2 === 'function' ? goLedger2 : undefined; },
  "goNewPlanner": function () { return typeof goNewPlanner === 'function' ? goNewPlanner : undefined; },
  "goPlanStep": function () { return typeof goPlanStep === 'function' ? goPlanStep : undefined; },
  "goRefresh": function () { return typeof goRefresh === 'function' ? goRefresh : undefined; },
  "goResumePlanner": function () { return typeof goResumePlanner === 'function' ? goResumePlanner : undefined; },
  "goSlide": function () { return typeof goSlide === 'function' ? goSlide : undefined; },
  "goToLedgerFromMap": function () { return typeof goToLedgerFromMap === 'function' ? goToLedgerFromMap : undefined; },
  "goToPlaceReviews": function () { return typeof goToPlaceReviews === 'function' ? goToPlaceReviews : undefined; },
  "goToScrapPlace": function () { return typeof goToScrapPlace === 'function' ? goToScrapPlace : undefined; },
  "goToScrapRoute": function () { return typeof goToScrapRoute === 'function' ? goToScrapRoute : undefined; },
  "gotoSite": function () { return typeof gotoSite === 'function' ? gotoSite : undefined; },
  "handleCommunitySideTag": function () { return typeof handleCommunitySideTag === 'function' ? handleCommunitySideTag : undefined; },
  "handleEditImageSelect": function () { return typeof handleEditImageSelect === 'function' ? handleEditImageSelect : undefined; },
  "handleEditImg": function () { return typeof handleEditImg === 'function' ? handleEditImg : undefined; },
  "handleImageSelect": function () { return typeof handleImageSelect === 'function' ? handleImageSelect : undefined; },
  "handleWriteImg": function () { return typeof handleWriteImg === 'function' ? handleWriteImg : undefined; },
  "initAdminPage": function () { return typeof initAdminPage === 'function' ? initAdminPage : undefined; },
  "initAuthPage": function () { return typeof initAuthPage === 'function' ? initAuthPage : undefined; },
  "initCommunityPage": function () { return typeof initCommunityPage === 'function' ? initCommunityPage : undefined; },
  "initCommunityPageData": function () { return typeof initCommunityPageData === 'function' ? initCommunityPageData : undefined; },
  "initDragAndDrop": function () { return typeof initDragAndDrop === 'function' ? initDragAndDrop : undefined; },
  "initKakaoMap": function () { return typeof initKakaoMap === 'function' ? initKakaoMap : undefined; },
  "initLedgerSelector": function () { return typeof initLedgerSelector === 'function' ? initLedgerSelector : undefined; },
  "initMainPage": function () { return typeof initMainPage === 'function' ? initMainPage : undefined; },
  "initMapPage": function () { return typeof initMapPage === 'function' ? initMapPage : undefined; },
  "initMobCarousels": function () { return typeof initMobCarousels === 'function' ? initMobCarousels : undefined; },
  "initMyPageData": function () { return typeof initMyPageData === 'function' ? initMyPageData : undefined; },
  "initPlannerDateConstraints": function () { return typeof initPlannerDateConstraints === 'function' ? initPlannerDateConstraints : undefined; },
  "initPlannerMBTI": function () { return typeof initPlannerMBTI === 'function' ? initPlannerMBTI : undefined; },
  "initPreviewMap": function () { return typeof initPreviewMap === 'function' ? initPreviewMap : undefined; },
  "initWeatherTab": function () { return typeof initWeatherTab === 'function' ? initWeatherTab : undefined; },
  "initWithdrawSection": function () { return typeof initWithdrawSection === 'function' ? initWithdrawSection : undefined; },
  "injectWritePlanSelect": function () { return typeof injectWritePlanSelect === 'function' ? injectWritePlanSelect : undefined; },
  "inputToStyleTags": function () { return typeof inputToStyleTags === 'function' ? inputToStyleTags : undefined; },
  "insertEditImage": function () { return typeof insertEditImage === 'function' ? insertEditImage : undefined; },
  "insertImagesIntoEditor": function () { return typeof insertImagesIntoEditor === 'function' ? insertImagesIntoEditor : undefined; },
  "installCommunityV2SearchAndSort": function () { return typeof installCommunityV2SearchAndSort === 'function' ? installCommunityV2SearchAndSort : undefined; },
  "inviteShareMember": function () { return typeof inviteShareMember === 'function' ? inviteShareMember : undefined; },
  "isCurrentUserPostOwnerForReviewEdit": function () { return typeof isCurrentUserPostOwnerForReviewEdit === 'function' ? isCurrentUserPostOwnerForReviewEdit : undefined; },
  "isSelectionInsideEditor": function () { return typeof isSelectionInsideEditor === 'function' ? isSelectionInsideEditor : undefined; },
  "layoutMapLabels": function () { return typeof layoutMapLabels === 'function' ? layoutMapLabels : undefined; },
  "likePost": function () { return typeof likePost === 'function' ? likePost : undefined; },
  "loadAdminCurations": function () { return typeof loadAdminCurations === 'function' ? loadAdminCurations : undefined; },
  "loadAdminDashboard": function () { return typeof loadAdminDashboard === 'function' ? loadAdminDashboard : undefined; },
  "loadAdminReports": function () { return typeof loadAdminReports === 'function' ? loadAdminReports : undefined; },
  "loadAdminStatistics": function () { return typeof loadAdminStatistics === 'function' ? loadAdminStatistics : undefined; },
  "loadAdminUsers": function () { return typeof loadAdminUsers === 'function' ? loadAdminUsers : undefined; },
  "loadComments": function () { return typeof loadComments === 'function' ? loadComments : undefined; },
  "loadCommunityPosts": function () { return typeof loadCommunityPosts === 'function' ? loadCommunityPosts : undefined; },
  "loadCommunitySidePanels": function () { return typeof loadCommunitySidePanels === 'function' ? loadCommunitySidePanels : undefined; },
  "loadCommunitySourcePlanDetail": function () { return typeof loadCommunitySourcePlanDetail === 'function' ? loadCommunitySourcePlanDetail : undefined; },
  "loadDemoMode": function () { return typeof loadDemoMode === 'function' ? loadDemoMode : undefined; },
  "loadMyReviews": function () { return typeof loadMyReviews === 'function' ? loadMyReviews : undefined; },
  "loadMyRouteScrap": function () { return typeof loadMyRouteScrap === 'function' ? loadMyRouteScrap : undefined; },
  "loadMyScrap": function () { return typeof loadMyScrap === 'function' ? loadMyScrap : undefined; },
  "loadPageCSS": function () { return typeof loadPageCSS === 'function' ? loadPageCSS : undefined; },
  "loadPlaceCards": function () { return typeof loadPlaceCards === 'function' ? loadPlaceCards : undefined; },
  "loadPlaceReviews": function () { return typeof loadPlaceReviews === 'function' ? loadPlaceReviews : undefined; },
  "loadPlaceReviewsSorted": function () { return typeof loadPlaceReviewsSorted === 'function' ? loadPlaceReviewsSorted : undefined; },
  "loadPlaceSearch": function () { return typeof loadPlaceSearch === 'function' ? loadPlaceSearch : undefined; },
  "loadPreviousPreference": function () { return typeof loadPreviousPreference === 'function' ? loadPreviousPreference : undefined; },
  "loadReviewDetail": function () { return typeof loadReviewDetail === 'function' ? loadReviewDetail : undefined; },
  "loadRouteSearchOrSorted": function () { return typeof loadRouteSearchOrSorted === 'function' ? loadRouteSearchOrSorted : undefined; },
  "loadShareMembersData": function () { return typeof loadShareMembersData === 'function' ? loadShareMembersData : undefined; },
  "loadShareModal": function () { return typeof loadShareModal === 'function' ? loadShareModal : undefined; },
  "loadWritePlanOptions": function () { return typeof loadWritePlanOptions === 'function' ? loadWritePlanOptions : undefined; },
  "markPlanDirty": function () { return typeof markPlanDirty === 'function' ? markPlanDirty : undefined; },
  "matchesPlaceBySearchType": function () { return typeof matchesPlaceBySearchType === 'function' ? matchesPlaceBySearchType : undefined; },
  "navPlannerClick": function () { return typeof navPlannerClick === 'function' ? navPlannerClick : undefined; },
  "normalizeCommunityExtraNotes": function () { return typeof normalizeCommunityExtraNotes === 'function' ? normalizeCommunityExtraNotes : undefined; },
  "normalizeCommunityPlannerDestination": function () { return typeof normalizeCommunityPlannerDestination === 'function' ? normalizeCommunityPlannerDestination : undefined; },
  "normalizeMyPageListResponse": function () { return typeof normalizeMyPageListResponse === 'function' ? normalizeMyPageListResponse : undefined; },
  "normalizeText": function () { return typeof normalizeText === 'function' ? normalizeText : undefined; },
  "notifIcon": function () { return typeof notifIcon === 'function' ? notifIcon : undefined; },
  "offsetSeg": function () { return typeof offsetSeg === 'function' ? offsetSeg : undefined; },
  "onEndDateChange": function () { return typeof onEndDateChange === 'function' ? onEndDateChange : undefined; },
  "onOtherStayInput": function () { return typeof onOtherStayInput === 'function' ? onOtherStayInput : undefined; },
  "onStartDateChange": function () { return typeof onStartDateChange === 'function' ? onStartDateChange : undefined; },
  "onTopPriceInput": function () { return typeof onTopPriceInput === 'function' ? onTopPriceInput : undefined; },
  "onWritePlanSelect": function () { return typeof onWritePlanSelect === 'function' ? onWritePlanSelect : undefined; },
  "openAdminPostDetail": function () { return typeof openAdminPostDetail === 'function' ? openAdminPostDetail : undefined; },
  "openAdminSec": function () { return typeof openAdminSec === 'function' ? openAdminSec : undefined; },
  "openCommunityPlaceSummary": function () { return typeof openCommunityPlaceSummary === 'function' ? openCommunityPlaceSummary : undefined; },
  "openCommunityPlanPreview": function () { return typeof openCommunityPlanPreview === 'function' ? openCommunityPlanPreview : undefined; },
  "openConfirmDone": function () { return typeof openConfirmDone === 'function' ? openConfirmDone : undefined; },
  "openCurrentPostEditModal": function () { return typeof openCurrentPostEditModal === 'function' ? openCurrentPostEditModal : undefined; },
  "openEditCommentModal": function () { return typeof openEditCommentModal === 'function' ? openEditCommentModal : undefined; },
  "openEditCuration": function () { return typeof openEditCuration === 'function' ? openEditCuration : undefined; },
  "openKakaoNavi": function () { return typeof openKakaoNavi === 'function' ? openKakaoNavi : undefined; },
  "openMapWeatherPopup": function () { return typeof openMapWeatherPopup === 'function' ? openMapWeatherPopup : undefined; },
  "openModal": function () { return typeof openModal === 'function' ? openModal : undefined; },
  "openMyReviewEdit": function () { return typeof openMyReviewEdit === 'function' ? openMyReviewEdit : undefined; },
  "openMyTrip": function () { return typeof openMyTrip === 'function' ? openMyTrip : undefined; },
  "openNotificationPopup": function () { return typeof openNotificationPopup === 'function' ? openNotificationPopup : undefined; },
  "openPlaceReviews": function () { return typeof openPlaceReviews === 'function' ? openPlaceReviews : undefined; },
  "openPostDetail": function () { return typeof openPostDetail === 'function' ? openPostDetail : undefined; },
  "openPreview": function () { return typeof openPreview === 'function' ? openPreview : undefined; },
  "openPriceModal": function () { return typeof openPriceModal === 'function' ? openPriceModal : undefined; },
  "openPwReset": function () { return typeof openPwReset === 'function' ? openPwReset : undefined; },
  "openPwResetModal": function () { return typeof openPwResetModal === 'function' ? openPwResetModal : undefined; },
  "openQueue": function () { return typeof openQueue === 'function' ? openQueue : undefined; },
  "openReportAction": function () { return typeof openReportAction === 'function' ? openReportAction : undefined; },
  "openReportCommentModal": function () { return typeof openReportCommentModal === 'function' ? openReportCommentModal : undefined; },
  "openReportPostModal": function () { return typeof openReportPostModal === 'function' ? openReportPostModal : undefined; },
  "openShareModal": function () { return typeof openShareModal === 'function' ? openShareModal : undefined; },
  "openSiteModal": function () { return typeof openSiteModal === 'function' ? openSiteModal : undefined; },
  "openSummaryEdit": function () { return typeof openSummaryEdit === 'function' ? openSummaryEdit : undefined; },
  "openSuspendModal": function () { return typeof openSuspendModal === 'function' ? openSuspendModal : undefined; },
  "openWeatherPopup": function () { return typeof openWeatherPopup === 'function' ? openWeatherPopup : undefined; },
  "openWriteImagePicker": function () { return typeof openWriteImagePicker === 'function' ? openWriteImagePicker : undefined; },
  "openWriteModal": function () { return typeof openWriteModal === 'function' ? openWriteModal : undefined; },
  "parseJsonArraySafe": function () { return typeof parseJsonArraySafe === 'function' ? parseJsonArraySafe : undefined; },
  "parseRouteData": function () { return typeof parseRouteData === 'function' ? parseRouteData : undefined; },
  "parseShareTripId": function () { return typeof parseShareTripId === 'function' ? parseShareTripId : undefined; },
  "parseStyleTags": function () { return typeof parseStyleTags === 'function' ? parseStyleTags : undefined; },
  "parseTags": function () { return typeof parseTags === 'function' ? parseTags : undefined; },
  "patchSetCommTab": function () { return typeof patchSetCommTab === 'function' ? patchSetCommTab : undefined; },
  "pick": function () { return typeof pick === 'function' ? pick : undefined; },
  "pickVis": function () { return typeof pickVis === 'function' ? pickVis : undefined; },
  "placeCaretAt": function () { return typeof placeCaretAt === 'function' ? placeCaretAt : undefined; },
  "prChangePassword": function () { return typeof prChangePassword === 'function' ? prChangePassword : undefined; },
  "prSendCode": function () { return typeof prSendCode === 'function' ? prSendCode : undefined; },
  "prSetMsg": function () { return typeof prSetMsg === 'function' ? prSetMsg : undefined; },
  "prShowStep": function () { return typeof prShowStep === 'function' ? prShowStep : undefined; },
  "prVerifyCode": function () { return typeof prVerifyCode === 'function' ? prVerifyCode : undefined; },
  "reSyncChipFromUpdate": function () { return typeof reSyncChipFromUpdate === 'function' ? reSyncChipFromUpdate : undefined; },
  "recalcDayBudget": function () { return typeof recalcDayBudget === 'function' ? recalcDayBudget : undefined; },
  "redrawDayRoute": function () { return typeof redrawDayRoute === 'function' ? redrawDayRoute : undefined; },
  "refreshAccessToken": function () { return typeof refreshAccessToken === 'function' ? refreshAccessToken : undefined; },
  "removeBodyInlineTags": function () { return typeof removeBodyInlineTags === 'function' ? removeBodyInlineTags : undefined; },
  "removeCommunityV2Pager": function () { return typeof removeCommunityV2Pager === 'function' ? removeCommunityV2Pager : undefined; },
  "removeDay": function () { return typeof removeDay === 'function' ? removeDay : undefined; },
  "removeGlobalLegacyPager": function () { return typeof removeGlobalLegacyPager === 'function' ? removeGlobalLegacyPager : undefined; },
  "removeImagesInSelection": function () { return typeof removeImagesInSelection === 'function' ? removeImagesInSelection : undefined; },
  "removeInlineImageElement": function () { return typeof removeInlineImageElement === 'function' ? removeInlineImageElement : undefined; },
  "removeInlineImageRecord": function () { return typeof removeInlineImageRecord === 'function' ? removeInlineImageRecord : undefined; },
  "removePlacePager": function () { return typeof removePlacePager === 'function' ? removePlacePager : undefined; },
  "removePlaceSearchPager": function () { return typeof removePlaceSearchPager === 'function' ? removePlaceSearchPager : undefined; },
  "removeQueue": function () { return typeof removeQueue === 'function' ? removeQueue : undefined; },
  "removeRouteSearchPager": function () { return typeof removeRouteSearchPager === 'function' ? removeRouteSearchPager : undefined; },
  "renderActualKakaoMap": function () { return typeof renderActualKakaoMap === 'function' ? renderActualKakaoMap : undefined; },
  "renderAdminPagination": function () { return typeof renderAdminPagination === 'function' ? renderAdminPagination : undefined; },
  "renderAiRecommendations": function () { return typeof renderAiRecommendations === 'function' ? renderAiRecommendations : undefined; },
  "renderArrows": function () { return typeof renderArrows === 'function' ? renderArrows : undefined; },
  "renderCommunityV2Pager": function () { return typeof renderCommunityV2Pager === 'function' ? renderCommunityV2Pager : undefined; },
  "renderDestBars": function () { return typeof renderDestBars === 'function' ? renderDestBars : undefined; },
  "renderDetailCategory": function () { return typeof renderDetailCategory === 'function' ? renderDetailCategory : undefined; },
  "renderDetailMeta": function () { return typeof renderDetailMeta === 'function' ? renderDetailMeta : undefined; },
  "renderDonut": function () { return typeof renderDonut === 'function' ? renderDonut : undefined; },
  "renderDynamicMockData": function () { return typeof renderDynamicMockData === 'function' ? renderDynamicMockData : undefined; },
  "renderEditImages": function () { return typeof renderEditImages === 'function' ? renderEditImages : undefined; },
  "renderLinkedPlan": function () { return typeof renderLinkedPlan === 'function' ? renderLinkedPlan : undefined; },
  "renderMyCommunityPagedList": function () { return typeof renderMyCommunityPagedList === 'function' ? renderMyCommunityPagedList : undefined; },
  "renderMyCommunityPager": function () { return typeof renderMyCommunityPager === 'function' ? renderMyCommunityPager : undefined; },
  "renderMyLikedReviewCard": function () { return typeof renderMyLikedReviewCard === 'function' ? renderMyLikedReviewCard : undefined; },
  "renderMyPlaceScrapCard": function () { return typeof renderMyPlaceScrapCard === 'function' ? renderMyPlaceScrapCard : undefined; },
  "renderMyReviewCard": function () { return typeof renderMyReviewCard === 'function' ? renderMyReviewCard : undefined; },
  "renderMyRouteScrapActions": function () { return typeof renderMyRouteScrapActions === 'function' ? renderMyRouteScrapActions : undefined; },
  "renderMyRouteScrapCard": function () { return typeof renderMyRouteScrapCard === 'function' ? renderMyRouteScrapCard : undefined; },
  "renderNotifList": function () { return typeof renderNotifList === 'function' ? renderNotifList : undefined; },
  "renderPlaceCardsPage": function () { return typeof renderPlaceCardsPage === 'function' ? renderPlaceCardsPage : undefined; },
  "renderPlaceDetailPage": function () { return typeof renderPlaceDetailPage === 'function' ? renderPlaceDetailPage : undefined; },
  "renderPlacePager": function () { return typeof renderPlacePager === 'function' ? renderPlacePager : undefined; },
  "renderPlaceReviewList": function () { return typeof renderPlaceReviewList === 'function' ? renderPlaceReviewList : undefined; },
  "renderPlaceSearchPager": function () { return typeof renderPlaceSearchPager === 'function' ? renderPlaceSearchPager : undefined; },
  "renderPlaceSnapshot": function () { return typeof renderPlaceSnapshot === 'function' ? renderPlaceSnapshot : undefined; },
  "renderPlanBadge": function () { return typeof renderPlanBadge === 'function' ? renderPlanBadge : undefined; },
  "renderPopularTags": function () { return typeof renderPopularTags === 'function' ? renderPopularTags : undefined; },
  "renderPostDetail": function () { return typeof renderPostDetail === 'function' ? renderPostDetail : undefined; },
  "renderPreviewInfo": function () { return typeof renderPreviewInfo === 'function' ? renderPreviewInfo : undefined; },
  "renderPreviewList": function () { return typeof renderPreviewList === 'function' ? renderPreviewList : undefined; },
  "renderQ": function () { return typeof renderQ === 'function' ? renderQ : undefined; },
  "renderReviewComments": function () { return typeof renderReviewComments === 'function' ? renderReviewComments : undefined; },
  "renderReviewDetailPage": function () { return typeof renderReviewDetailPage === 'function' ? renderReviewDetailPage : undefined; },
  "renderRouteResultPage": function () { return typeof renderRouteResultPage === 'function' ? renderRouteResultPage : undefined; },
  "renderRouteSearchPager": function () { return typeof renderRouteSearchPager === 'function' ? renderRouteSearchPager : undefined; },
  "renderStars": function () { return typeof renderStars === 'function' ? renderStars : undefined; },
  "renderTagsBeforePlan": function () { return typeof renderTagsBeforePlan === 'function' ? renderTagsBeforePlan : undefined; },
  "renderTrendChart": function () { return typeof renderTrendChart === 'function' ? renderTrendChart : undefined; },
  "renderWeatherTab": function () { return typeof renderWeatherTab === 'function' ? renderWeatherTab : undefined; },
  "requestJson": function () { return typeof requestJson === 'function' ? requestJson : undefined; },
  "requestMapIndoorAlternative": function () { return typeof requestMapIndoorAlternative === 'function' ? requestMapIndoorAlternative : undefined; },
  "requestPlaceCardList": function () { return typeof requestPlaceCardList === 'function' ? requestPlaceCardList : undefined; },
  "requireLogin": function () { return typeof requireLogin === 'function' ? requireLogin : undefined; },
  "resetAndBackToStep1": function () { return typeof resetAndBackToStep1 === 'function' ? resetAndBackToStep1 : undefined; },
  "resetCommunityV2SearchToFullList": function () { return typeof resetCommunityV2SearchToFullList === 'function' ? resetCommunityV2SearchToFullList : undefined; },
  "resetCurationForm": function () { return typeof resetCurationForm === 'function' ? resetCurationForm : undefined; },
  "resetEditorCommandState": function () { return typeof resetEditorCommandState === 'function' ? resetEditorCommandState : undefined; },
  "resetInfoStep": function () { return typeof resetInfoStep === 'function' ? resetInfoStep : undefined; },
  "resetPlannerForm": function () { return typeof resetPlannerForm === 'function' ? resetPlannerForm : undefined; },
  "resolveCommunityPlannerDestination": function () { return typeof resolveCommunityPlannerDestination === 'function' ? resolveCommunityPlannerDestination : undefined; },
  "restoreCommunityReviewDetail": function () { return typeof restoreCommunityReviewDetail === 'function' ? restoreCommunityReviewDetail : undefined; },
  "restoreEditorSelection": function () { return typeof restoreEditorSelection === 'function' ? restoreEditorSelection : undefined; },
  "restorePlannerFromTrip": function () { return typeof restorePlannerFromTrip === 'function' ? restorePlannerFromTrip : undefined; },
  "returnToLedgerSelector": function () { return typeof returnToLedgerSelector === 'function' ? returnToLedgerSelector : undefined; },
  "rmQ": function () { return typeof rmQ === 'function' ? rmQ : undefined; },
  "sanitizePostContent": function () { return typeof sanitizePostContent === 'function' ? sanitizePostContent : undefined; },
  "saveCuration": function () { return typeof saveCuration === 'function' ? saveCuration : undefined; },
  "saveCurrentChanges": function () { return typeof saveCurrentChanges === 'function' ? saveCurrentChanges : undefined; },
  "saveEditExpense": function () { return typeof saveEditExpense === 'function' ? saveEditExpense : undefined; },
  "saveEditorSelection": function () { return typeof saveEditorSelection === 'function' ? saveEditorSelection : undefined; },
  "saveFormAndGoChat": function () { return typeof saveFormAndGoChat === 'function' ? saveFormAndGoChat : undefined; },
  "saveInfoEdit": function () { return typeof saveInfoEdit === 'function' ? saveInfoEdit : undefined; },
  "saveStayPrice": function () { return typeof saveStayPrice === 'function' ? saveStayPrice : undefined; },
  "scrapCurrentPreviewPlan": function () { return typeof scrapCurrentPreviewPlan === 'function' ? scrapCurrentPreviewPlan : undefined; },
  "scrapPost": function () { return typeof scrapPost === 'function' ? scrapPost : undefined; },
  "searchAdminUserByName": function () { return typeof searchAdminUserByName === 'function' ? searchAdminUserByName : undefined; },
  "searchPlaceByName": function () { return typeof searchPlaceByName === 'function' ? searchPlaceByName : undefined; },
  "selLedger": function () { return typeof selLedger === 'function' ? selLedger : undefined; },
  "selectInlineImage": function () { return typeof selectInlineImage === 'function' ? selectInlineImage : undefined; },
  "selectMbti": function () { return typeof selectMbti === 'function' ? selectMbti : undefined; },
  "sendAuthCode": function () { return typeof sendAuthCode === 'function' ? sendAuthCode : undefined; },
  "sendMsg": function () { return typeof sendMsg === 'function' ? sendMsg : undefined; },
  "sendPwEmail": function () { return typeof sendPwEmail === 'function' ? sendPwEmail : undefined; },
  "setButtonState": function () { return typeof setButtonState === 'function' ? setButtonState : undefined; },
  "setCommTab": function () { return typeof setCommTab === 'function' ? setCommTab : undefined; },
  "setEditorCommandButtonState": function () { return typeof setEditorCommandButtonState === 'function' ? setEditorCommandButtonState : undefined; },
  "setExpensePage": function () { return typeof setExpensePage === 'function' ? setExpensePage : undefined; },
  "setHtml": function () { return typeof setHtml === 'function' ? setHtml : undefined; },
  "setNav": function () { return typeof setNav === 'function' ? setNav : undefined; },
  "setPreviewScrapButtonHover": function () { return typeof setPreviewScrapButtonHover === 'function' ? setPreviewScrapButtonHover : undefined; },
  "setPreviewScrapButtonState": function () { return typeof setPreviewScrapButtonState === 'function' ? setPreviewScrapButtonState : undefined; },
  "setPwNew": function () { return typeof setPwNew === 'function' ? setPwNew : undefined; },
  "setShareTab": function () { return typeof setShareTab === 'function' ? setShareTab : undefined; },
  "setStars": function () { return typeof setStars === 'function' ? setStars : undefined; },
  "setText": function () { return typeof setText === 'function' ? setText : undefined; },
  "shareInviteToKakaoTalk": function () { return typeof shareInviteToKakaoTalk === 'function' ? shareInviteToKakaoTalk : undefined; },
  "showAdmin": function () { return typeof showAdmin === 'function' ? showAdmin : undefined; },
  "showConfirmModal": function () { return typeof showConfirmModal === 'function' ? showConfirmModal : undefined; },
  "showDay": function () { return typeof showDay === 'function' ? showDay : undefined; },
  "showMapPlacePopup": function () { return typeof showMapPlacePopup === 'function' ? showMapPlacePopup : undefined; },
  "showMySection": function () { return typeof showMySection === 'function' ? showMySection : undefined; },
  "showOtherInput": function () { return typeof showOtherInput === 'function' ? showOtherInput : undefined; },
  "showPlaceReviews": function () { return typeof showPlaceReviews === 'function' ? showPlaceReviews : undefined; },
  "showPwLimitToast": function () { return typeof showPwLimitToast === 'function' ? showPwLimitToast : undefined; },
  "showReplaceInput": function () { return typeof showReplaceInput === 'function' ? showReplaceInput : undefined; },
  "showReviewDetail": function () { return typeof showReviewDetail === 'function' ? showReviewDetail : undefined; },
  "showSocialInfoEdit": function () { return typeof showSocialInfoEdit === 'function' ? showSocialInfoEdit : undefined; },
  "snoozeAndClosePwChangeModal": function () { return typeof snoozeAndClosePwChangeModal === 'function' ? snoozeAndClosePwChangeModal : undefined; },
  "sortPosts": function () { return typeof sortPosts === 'function' ? sortPosts : undefined; },
  "sortReportTable": function () { return typeof sortReportTable === 'function' ? sortReportTable : undefined; },
  "sortRoutePosts": function () { return typeof sortRoutePosts === 'function' ? sortRoutePosts : undefined; },
  "sortUserTable": function () { return typeof sortUserTable === 'function' ? sortUserTable : undefined; },
  "starsHtml": function () { return typeof starsHtml === 'function' ? starsHtml : undefined; },
  "startChat": function () { return typeof startChat === 'function' ? startChat : undefined; },
  "startChatWithSummary": function () { return typeof startChatWithSummary === 'function' ? startChatWithSummary : undefined; },
  "startEditExpense": function () { return typeof startEditExpense === 'function' ? startEditExpense : undefined; },
  "startPlanFromCard": function () { return typeof startPlanFromCard === 'function' ? startPlanFromCard : undefined; },
  "startPlanFromCommunityPost": function () { return typeof startPlanFromCommunityPost === 'function' ? startPlanFromCommunityPost : undefined; },
  "startPlanFromCuration": function () { return typeof startPlanFromCuration === 'function' ? startPlanFromCuration : undefined; },
  "startPlannerFromCommunityPostObject": function () { return typeof startPlannerFromCommunityPostObject === 'function' ? startPlannerFromCommunityPostObject : undefined; },
  "startSocialSignup": function () { return typeof startSocialSignup === 'function' ? startSocialSignup : undefined; },
  "stripHash": function () { return typeof stripHash === 'function' ? stripHash : undefined; },
  "styleTagsToInput": function () { return typeof styleTagsToInput === 'function' ? styleTagsToInput : undefined; },
  "submitComment": function () { return typeof submitComment === 'function' ? submitComment : undefined; },
  "submitEditComment": function () { return typeof submitEditComment === 'function' ? submitEditComment : undefined; },
  "submitEditPost": function () { return typeof submitEditPost === 'function' ? submitEditPost : undefined; },
  "submitEditReview": function () { return typeof submitEditReview === 'function' ? submitEditReview : undefined; },
  "submitReportComment": function () { return typeof submitReportComment === 'function' ? submitReportComment : undefined; },
  "submitReportPost": function () { return typeof submitReportPost === 'function' ? submitReportPost : undefined; },
  "submitReview": function () { return typeof submitReview === 'function' ? submitReview : undefined; },
  "sv": function () { return typeof sv === 'function' ? sv : undefined; },
  "switchLedgerTab": function () { return typeof switchLedgerTab === 'function' ? switchLedgerTab : undefined; },
  "switchMapTab": function () { return typeof switchMapTab === 'function' ? switchMapTab : undefined; },
  "switchMobileMapTab": function () { return typeof switchMobileMapTab === 'function' ? switchMobileMapTab : undefined; },
  "switchSumTab": function () { return typeof switchSumTab === 'function' ? switchSumTab : undefined; },
  "syncDetailScrapButtonVisual": function () { return typeof syncDetailScrapButtonVisual === 'function' ? syncDetailScrapButtonVisual : undefined; },
  "syncEditorTypingState": function () { return typeof syncEditorTypingState === 'function' ? syncEditorTypingState : undefined; },
  "syncNavHeight": function () { return typeof syncNavHeight === 'function' ? syncNavHeight : undefined; },
  "syncPreviewScrapButtonState": function () { return typeof syncPreviewScrapButtonState === 'function' ? syncPreviewScrapButtonState : undefined; },
  "toDisplayItem": function () { return typeof toDisplayItem === 'function' ? toDisplayItem : undefined; },
  "toast": function () { return typeof toast === 'function' ? toast : undefined; },
  "tog": function () { return typeof tog === 'function' ? tog : undefined; },
  "togBtn": function () { return typeof togBtn === 'function' ? togBtn : undefined; },
  "toggleInvitedDeleteMode": function () { return typeof toggleInvitedDeleteMode === 'function' ? toggleInvitedDeleteMode : undefined; },
  "toggleMainPlaceScrap": function () { return typeof toggleMainPlaceScrap === 'function' ? toggleMainPlaceScrap : undefined; },
  "toggleMapLabels": function () { return typeof toggleMapLabels === 'function' ? toggleMapLabels : undefined; },
  "toggleMarker": function () { return typeof toggleMarker === 'function' ? toggleMarker : undefined; },
  "toggleMyMenu": function () { return typeof toggleMyMenu === 'function' ? toggleMyMenu : undefined; },
  "toggleMyRouteScrapDeleteMode": function () { return typeof toggleMyRouteScrapDeleteMode === 'function' ? toggleMyRouteScrapDeleteMode : undefined; },
  "toggleMyTripsDeleteMode": function () { return typeof toggleMyTripsDeleteMode === 'function' ? toggleMyTripsDeleteMode : undefined; },
  "toggleOtherStay": function () { return typeof toggleOtherStay === 'function' ? toggleOtherStay : undefined; },
  "togglePlanConfirm": function () { return typeof togglePlanConfirm === 'function' ? togglePlanConfirm : undefined; },
  "toggleRouteScrap": function () { return typeof toggleRouteScrap === 'function' ? toggleRouteScrap : undefined; },
  "tryDemoLogin": function () { return typeof tryDemoLogin === 'function' ? tryDemoLogin : undefined; },
  "tryGoogleLogin": function () { return typeof tryGoogleLogin === 'function' ? tryGoogleLogin : undefined; },
  "tryKakaoLogin": function () { return typeof tryKakaoLogin === 'function' ? tryKakaoLogin : undefined; },
  "tryLogin": function () { return typeof tryLogin === 'function' ? tryLogin : undefined; },
  "unsuspendUser": function () { return typeof unsuspendUser === 'function' ? unsuspendUser : undefined; },
  "updateBoundsForDay": function () { return typeof updateBoundsForDay === 'function' ? updateBoundsForDay : undefined; },
  "updateCity": function () { return typeof updateCity === 'function' ? updateCity : undefined; },
  "updateCityDep": function () { return typeof updateCityDep === 'function' ? updateCityDep : undefined; },
  "updateCityDest": function () { return typeof updateCityDest === 'function' ? updateCityDest : undefined; },
  "updateCurDestCity": function () { return typeof updateCurDestCity === 'function' ? updateCurDestCity : undefined; },
  "updateDemoNotice": function () { return typeof updateDemoNotice === 'function' ? updateDemoNotice : undefined; },
  "updateEditorToolbarState": function () { return typeof updateEditorToolbarState === 'function' ? updateEditorToolbarState : undefined; },
  "updateLedgerList": function () { return typeof updateLedgerList === 'function' ? updateLedgerList : undefined; },
  "updateMyPageUI": function () { return typeof updateMyPageUI === 'function' ? updateMyPageUI : undefined; },
  "updateNav": function () { return typeof updateNav === 'function' ? updateNav : undefined; },
  "updateNotifBadge": function () { return typeof updateNotifBadge === 'function' ? updateNotifBadge : undefined; },
  "updateQueueReq": function () { return typeof updateQueueReq === 'function' ? updateQueueReq : undefined; },
  "updateReviewEditButtonForModal": function () { return typeof updateReviewEditButtonForModal === 'function' ? updateReviewEditButtonForModal : undefined; },
  "updateStarPreview": function () { return typeof updateStarPreview === 'function' ? updateStarPreview : undefined; },
  "updateSummaryCard": function () { return typeof updateSummaryCard === 'function' ? updateSummaryCard : undefined; },
  "updateWeatherBtn": function () { return typeof updateWeatherBtn === 'function' ? updateWeatherBtn : undefined; },
  "uploadEditImages": function () { return typeof uploadEditImages === 'function' ? uploadEditImages : undefined; },
  "uploadPostImages": function () { return typeof uploadPostImages === 'function' ? uploadPostImages : undefined; },
  "validateAndHealingRoute": function () { return typeof validateAndHealingRoute === 'function' ? validateAndHealingRoute : undefined; },
  "verifyAuthCode": function () { return typeof verifyAuthCode === 'function' ? verifyAuthCode : undefined; },
  "verifyCode": function () { return typeof verifyCode === 'function' ? verifyCode : undefined; },
  "verifyInfoPw": function () { return typeof verifyInfoPw === 'function' ? verifyInfoPw : undefined; },
  "waitForInlineImageInsertions": function () { return typeof waitForInlineImageInsertions === 'function' ? waitForInlineImageInsertions : undefined; },
};
