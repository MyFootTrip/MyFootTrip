from allauth.account.adapter import DefaultAccountAdapter

class CustomAccountAdapter(DefaultAccountAdapter):
    def clean_username(self, username, shallow=False):
        
        return username
        
    def save_user(self, request, user, form, commit=False):
        user = super().save_user(request, user, form, commit)
        data = form.cleaned_data
        user.phone_number = data.get('phone_number')
        user.profileImg = data.get('profileImg')
        user.naver = data.get('naver')
        user.google = data.get('google')
        user.kakao = data.get('kakao')
        user.nickname = data.get('nickname')
        user.age = data.get('age')

        user.save()
        return user